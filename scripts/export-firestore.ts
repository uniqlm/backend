/**
 * Firestore Export Script
 * 
 * Exports all Firestore collections to JSON files for migration to PostgreSQL.
 * Subcollections are embedded as nested `_subcollections` objects.
 * 
 * Usage:
 *   cd apps/backend/scripts
 *   npm install
 *   npm run export
 * 
 * Prerequisites:
 *   1. Place your Firebase service account key as `serviceAccountKey.json` in this directory
 *   2. Or set GOOGLE_APPLICATION_CREDENTIALS environment variable
 */

import admin from 'firebase-admin';
import * as fs from 'fs';
import * as path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// ═══════════════════════════════════════════════════════════════════════════════
// CONFIGURATION
// ═══════════════════════════════════════════════════════════════════════════════

const CONFIG = {
    // Output directory for JSON files
    outputDir: path.join(__dirname, 'exports'),

    // Firebase project ID (optional if using service account)
    projectId: 'uniqlm-1',

    // Collections to export (empty = export all root collections)
    collectionsToExport: [] as string[],

    // Known subcollections to look for (add more as needed)
    // This helps with performance - we only check for these subcollections
    knownSubcollections: [
        'coinBalance',
        'transactions',
        'data',
        'subscription',
        'progress',
        'notes',
        'bookmarks'
    ],

    // Maximum documents per collection (0 = no limit)
    maxDocsPerCollection: 0,

    // Pretty print JSON output
    prettyPrint: true
};

// ═══════════════════════════════════════════════════════════════════════════════
// INITIALIZATION
// ═══════════════════════════════════════════════════════════════════════════════

function initializeFirebase(): admin.firestore.Firestore {
    const serviceAccountPath = path.join(__dirname, 'serviceAccountKey.json');
    const adcPath = path.join(process.env.HOME || '', '.config/gcloud/application_default_credentials.json');

    // Option 1: Use service account key file if it exists
    if (fs.existsSync(serviceAccountPath)) {
        const serviceAccount = JSON.parse(fs.readFileSync(serviceAccountPath, 'utf-8'));
        admin.initializeApp({
            credential: admin.credential.cert(serviceAccount),
            projectId: CONFIG.projectId
        });
        console.log('✓ Initialized with service account key');
    }
    // Option 2: Use Application Default Credentials file from gcloud
    else if (fs.existsSync(adcPath)) {
        // Set the env var so firebase-admin can find it
        process.env.GOOGLE_APPLICATION_CREDENTIALS = adcPath;
        admin.initializeApp({
            projectId: CONFIG.projectId
        });
        console.log('✓ Initialized with Application Default Credentials (gcloud)');
        console.log(`   Using: ${adcPath}`);
    }
    else {
        console.error('❌ No credentials found!');
        console.error('');
        console.error('   Run this command to authenticate:');
        console.error('');
        console.error('   gcloud auth application-default login');
        console.error('');
        console.error('   This will open a browser to login with your Google account.');
        process.exit(1);
    }

    return admin.firestore();
}

// ═══════════════════════════════════════════════════════════════════════════════
// EXPORT FUNCTIONS
// ═══════════════════════════════════════════════════════════════════════════════

interface ExportedDocument {
    _id: string;
    _path: string;
    [key: string]: any;
    _subcollections?: Record<string, ExportedDocument[]>;
}

interface ExportStats {
    collection: string;
    documentCount: number;
    subcollectionCounts: Record<string, number>;
}

/**
 * Convert Firestore data types to JSON-serializable format
 */
function serializeValue(value: any): any {
    if (value === null || value === undefined) {
        return null;
    }

    if (value instanceof admin.firestore.Timestamp) {
        return {
            _type: 'timestamp',
            _seconds: value.seconds,
            _nanoseconds: value.nanoseconds,
            isoString: value.toDate().toISOString()
        };
    }

    if (value instanceof admin.firestore.GeoPoint) {
        return {
            _type: 'geopoint',
            latitude: value.latitude,
            longitude: value.longitude
        };
    }

    if (value instanceof admin.firestore.DocumentReference) {
        return {
            _type: 'reference',
            path: value.path
        };
    }

    if (Array.isArray(value)) {
        return value.map(serializeValue);
    }

    if (typeof value === 'object') {
        const serialized: Record<string, any> = {};
        for (const [k, v] of Object.entries(value)) {
            serialized[k] = serializeValue(v);
        }
        return serialized;
    }

    return value;
}

/**
 * Export subcollections for a document
 */
async function exportSubcollections(
    docRef: admin.firestore.DocumentReference
): Promise<Record<string, ExportedDocument[]>> {
    const subcollections: Record<string, ExportedDocument[]> = {};

    for (const subName of CONFIG.knownSubcollections) {
        const subCollectionRef = docRef.collection(subName);
        const subSnapshot = await subCollectionRef.get();

        if (!subSnapshot.empty) {
            subcollections[subName] = [];

            for (const subDoc of subSnapshot.docs) {
                const subData = serializeValue(subDoc.data());
                subcollections[subName].push({
                    _id: subDoc.id,
                    _path: subDoc.ref.path,
                    ...subData
                });
            }

            console.log(`      └─ ${subName}: ${subSnapshot.size} documents`);
        }
    }

    return Object.keys(subcollections).length > 0 ? subcollections : {};
}

/**
 * Export a single collection
 */
async function exportCollection(
    db: admin.firestore.Firestore,
    collectionName: string
): Promise<{ documents: ExportedDocument[]; stats: ExportStats }> {
    console.log(`\n📁 Exporting collection: ${collectionName}`);

    const collectionRef = db.collection(collectionName);
    let query: admin.firestore.Query = collectionRef;

    if (CONFIG.maxDocsPerCollection > 0) {
        query = query.limit(CONFIG.maxDocsPerCollection);
    }

    const snapshot = await query.get();
    console.log(`   Found ${snapshot.size} documents`);

    const documents: ExportedDocument[] = [];
    const stats: ExportStats = {
        collection: collectionName,
        documentCount: snapshot.size,
        subcollectionCounts: {}
    };

    let processedCount = 0;
    for (const doc of snapshot.docs) {
        processedCount++;
        const data = serializeValue(doc.data());

        const exportedDoc: ExportedDocument = {
            _id: doc.id,
            _path: doc.ref.path,
            ...data
        };

        // Export subcollections
        const subcollections = await exportSubcollections(doc.ref);
        if (Object.keys(subcollections).length > 0) {
            exportedDoc._subcollections = subcollections;

            // Update stats
            for (const [subName, subDocs] of Object.entries(subcollections)) {
                stats.subcollectionCounts[subName] =
                    (stats.subcollectionCounts[subName] || 0) + subDocs.length;
            }
        }

        documents.push(exportedDoc);

        // Progress indicator
        if (processedCount % 100 === 0) {
            console.log(`   Processed ${processedCount}/${snapshot.size} documents...`);
        }
    }

    return { documents, stats };
}

/**
 * Get all root collection names
 */
async function getRootCollections(db: admin.firestore.Firestore): Promise<string[]> {
    const collections = await db.listCollections();
    return collections.map(col => col.id);
}

/**
 * Write documents to JSON file
 */
function writeToFile(collectionName: string, documents: ExportedDocument[]): void {
    // Ensure output directory exists
    if (!fs.existsSync(CONFIG.outputDir)) {
        fs.mkdirSync(CONFIG.outputDir, { recursive: true });
    }

    const filePath = path.join(CONFIG.outputDir, `${collectionName}.json`);
    const content = CONFIG.prettyPrint
        ? JSON.stringify(documents, null, 2)
        : JSON.stringify(documents);

    fs.writeFileSync(filePath, content, 'utf-8');
    console.log(`   ✓ Saved to ${filePath}`);
}

// ═══════════════════════════════════════════════════════════════════════════════
// MAIN
// ═══════════════════════════════════════════════════════════════════════════════

async function main(): Promise<void> {
    console.log('═══════════════════════════════════════════════════════════════');
    console.log('           Firestore Export Tool');
    console.log('═══════════════════════════════════════════════════════════════');
    console.log(`Project: ${CONFIG.projectId}`);
    console.log(`Output:  ${CONFIG.outputDir}`);
    console.log('');

    const db = initializeFirebase();

    // Get collections to export
    let collectionsToExport = CONFIG.collectionsToExport;
    if (collectionsToExport.length === 0) {
        console.log('🔍 Discovering root collections...');
        collectionsToExport = await getRootCollections(db);
        console.log(`   Found ${collectionsToExport.length} collections: ${collectionsToExport.join(', ')}`);
    }

    const allStats: ExportStats[] = [];
    const startTime = Date.now();

    // Export each collection
    for (const collectionName of collectionsToExport) {
        try {
            const { documents, stats } = await exportCollection(db, collectionName);
            writeToFile(collectionName, documents);
            allStats.push(stats);
        } catch (error) {
            console.error(`   ❌ Error exporting ${collectionName}:`, error);
        }
    }

    const duration = ((Date.now() - startTime) / 1000).toFixed(2);

    // Print summary
    console.log('\n═══════════════════════════════════════════════════════════════');
    console.log('                      EXPORT SUMMARY');
    console.log('═══════════════════════════════════════════════════════════════');
    console.log(`Duration: ${duration}s\n`);

    let totalDocs = 0;
    let totalSubDocs = 0;

    for (const stat of allStats) {
        totalDocs += stat.documentCount;
        const subDocCount = Object.values(stat.subcollectionCounts).reduce((a, b) => a + b, 0);
        totalSubDocs += subDocCount;

        console.log(`📁 ${stat.collection}`);
        console.log(`   Documents: ${stat.documentCount}`);
        if (Object.keys(stat.subcollectionCounts).length > 0) {
            console.log(`   Subcollections:`);
            for (const [subName, count] of Object.entries(stat.subcollectionCounts)) {
                console.log(`     └─ ${subName}: ${count}`);
            }
        }
    }

    console.log('\n───────────────────────────────────────────────────────────────');
    console.log(`TOTAL: ${totalDocs} documents + ${totalSubDocs} subdocuments`);
    console.log(`Files saved to: ${CONFIG.outputDir}`);
    console.log('═══════════════════════════════════════════════════════════════');

    process.exit(0);
}

main().catch((error) => {
    console.error('Fatal error:', error);
    process.exit(1);
});
