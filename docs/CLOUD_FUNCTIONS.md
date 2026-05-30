# Kalkan Cloud Functions

## Function

- **Name:** `sendAnnouncementPush`
- **Trigger:** `announcements/{announcementId}` onCreate (Firestore)
- **Runtime:** Node.js 20, TypeScript 5.7

- **Name:** `sendFamilySafetyAlertPush`
- **Trigger:** `safety_status/{statusId}` onCreate (Firestore)
- **Purpose:** Sends high-priority `sos` and `need_help` alerts to the other users in the same family group.

## Deploy

```bash
cd functions
npm install
npm run build
cd ..
firebase login
firebase deploy --only functions:sendAnnouncementPush
```

SOS and help alerts:

```bash
firebase deploy --only functions:sendFamilySafetyAlertPush
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
