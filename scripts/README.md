# Firestore Export Scripts

Scripts for exporting Firestore data to JSON for migration to PostgreSQL.

## Setup

1. **Get Firebase Service Account Key:**
   - Go to [Firebase Console](https://console.firebase.google.com/project/uniqlm-1/settings/serviceaccounts/adminsdk)
   - Click "Generate new private key"
   - Save as `serviceAccountKey.json` in this directory

2. **Install dependencies:**
   ```bash
   npm install
   ```

## Usage

```bash
npm run export
```

This will:
1. Connect to Firestore using your service account
2. Discover all root collections
3. Export each collection to `exports/<collection>.json`
4. Embed subcollections as `_subcollections` in parent documents

## Output Format

```json
{
  "_id": "userId123",
  "_path": "users/userId123",
  "email": "user@example.com",
  "createdAt": {
    "_type": "timestamp",
    "isoString": "2026-01-01T00:00:00.000Z"
  },
  "_subcollections": {
    "coinBalance": [{ ... }],
    "transactions": [{ ... }]
  }
}
```

## Configuration

Edit the `CONFIG` object in `export-firestore.ts` to customize:
- `collectionsToExport` - Specific collections (empty = all)
- `knownSubcollections` - Subcollections to look for
- `maxDocsPerCollection` - Limit documents (0 = no limit)
