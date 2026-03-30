# Matriz Final de Rastreabilidade

Data: 2026-02-20  
Escopo: Entidade do banco -> endpoint OpenAPI -> request Postman

Legenda de cobertura:

- Direta: entidade é recurso explícito de API.
- Indireta: entidade é persistida/operada por fluxo de outro recurso.
- Parcial: há cobertura funcional, mas não CRUD completo da entidade.

| Entidade (DB) | Endpoints OpenAPI principais | Requests Postman principais | Cobertura |
|---|---|---|---|
| users | GET/PATCH /users/me, GET /auth/me, PATCH /admin/users/{userId}/status | Get Current User (me), Get Auth Me, Update Current User Profile, Update User Account Status | Direta |
| auth_identities | POST /auth/register, POST /auth/login | Register with Email, Login with Email | Indireta |
| verification_tokens | POST /auth/verification/request, POST /auth/verification/confirm, POST /auth/password/forgot, POST /auth/password/reset | Request Verification Token, Confirm Verification, Forgot Password, Reset Password | Indireta |
| devices | GET /devices/me, PATCH/DELETE /devices/{deviceId} | List My Devices, Update Device, Delete Device | Direta |
| circles | GET/POST /circles, GET/PATCH/DELETE /circles/{circleId} | List My Circles, Create Circle, Get Circle Details, Update Circle, Delete Circle | Direta |
| circle_members | GET /circles/{circleId}/members, POST /circles/{circleId}/join, POST /circles/invites/{inviteCode}/accept | List Circle Members, Join Circle with Invite Code, Accept Invite by Code | Direta |
| circle_invites | GET/POST /circles/{circleId}/invites, PATCH /circles/{circleId}/invites/{inviteId}, POST /circles/invites/{inviteCode}/accept | List Circle Invites, Create Circle Invite, Cancel Circle Invite, Accept Invite by Code | Direta |
| circle_settings | GET/PUT /circles/{circleId}/settings | Get Circle Settings, Update Circle Settings | Direta |
| locations | POST /locations/stream, GET /circles/{circleId}/members/{memberId}/location, GET /circles/{circleId}/members/{memberId}/locations/history | Send Location Stream, Get Member Last Location, Get Member Location History | Direta |
| location_sharing_states | GET/PUT /circles/{circleId}/sharing/me | Get My Sharing State, Update My Sharing State | Direta |
| places | GET/POST /circles/{circleId}/places, GET/PATCH/DELETE /circles/{circleId}/places/{placeId} | List Places, Create Place, Get Place Details, Update Place, Delete Place | Direta |
| place_alert_policies | GET/PUT /circles/{circleId}/places/{placeId}/alert-policies | Get Place Alert Policy, Update Place Alert Policy | Direta |
| place_alert_targets | PUT /circles/{circleId}/places/{placeId}/alert-policies (targetUserIds) | Update Place Alert Policy | Indireta |
| place_events | GET /circles/{circleId}/places/events | List Place Events | Direta |
| drives | GET /circles/{circleId}/members/{memberId}/drives, GET /drives/{driveId}, GET /circles/{circleId}/members/{memberId}/driving/summary | List Member Drives, Get Drive Details, Get Driving Summary | Direta |
| drive_events | GET /drives/{driveId}/events, GET /drives/{driveId}?includeEvents=true | Get Drive Events, Get Drive Details | Direta |
| sos_events | POST /sos, PATCH /sos/{sosId} | Trigger SOS, Update SOS Status | Direta |
| incident_detections | GET /incidents, GET /incidents/{incidentId} | List Incidents, Get Incident Details | Direta |
| circle_messages | GET/POST /circles/{circleId}/messages | List Circle Messages, Send Circle Message | Direta |
| circle_message_receipts | POST/GET /circles/{circleId}/messages/{messageId}/receipts | Send Read Receipt, Get Message Receipts | Direta |
| checkins | GET/POST /circles/{circleId}/checkins | List Check-ins, Create Check-in | Direta |
| notification_preferences | GET/PUT /notification-preferences | Get Notification Preferences, Update Notification Preferences | Direta |
| notifications | GET /notifications | List Notifications | Direta |
| plans | GET /plans, GET /plans/{planId} | List Plans, Get Plan Details | Direta |
| subscriptions | GET /subscriptions/me, POST /subscriptions | Get My Subscription, Create Subscription | Direta |
| admin_users | Endpoints protegidos em /admin/* (não há CRUD explícito de admin_users) | Search Users, Update User Account Status, Create User Flag, List Audit Logs | Parcial |
| user_flags | POST /admin/users/{userId}/flags | Create User Flag | Direta |
| audit_logs | GET /admin/audit-logs | List Audit Logs | Direta |

## Resumo Executivo

- Entidades totais do modelo: 28
- Cobertura Direta: 23
- Cobertura Indireta: 4
- Cobertura Parcial: 1

Observações:

- auth_identities e verification_tokens são entidades de infraestrutura de autenticação expostas por fluxos, não por recursos CRUD dedicados.
- place_alert_targets está embutida no payload de política de alertas (targetUserIds).
- admin_users está coberta funcionalmente por operações administrativas, porém sem endpoint dedicado para gestão do próprio cadastro admin.
