# Kalkan Cloud Functions

## Function

- **Name:** `sendAnnouncementPush`
- **Trigger:** `announcements/{announcementId}` onCreate (Firestore)
- **Runtime:** Node.js 20, TypeScript 5.7

## Deploy

```bash
cd functions
npm install
npm run build
cd ..
firebase login
firebase deploy --only functions:sendAnnouncementPush
```

Tüm functions:

```bash
firebase deploy --only functions
```

Loglar:

```bash
firebase functions:log --only sendAnnouncementPush
```

## Gereksinimler

- Firebase projesi: `kalkan-f116e`
- Blaze (pay-as-you-go) plan — FCM multicast için gerekli olabilir
- Firestore composite index: `status` + `createdAt` (Android okuma için)
