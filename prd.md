# PRD: Family Locator (FamilySafe 360) Plataforma Backend

## 1. Visão Geral do Produto

### 1.1 Título e Versão do Documento

- **Título do Documento:** PRD: Family Locator (FamilySafe 360) Plataforma Backend
- **Versão:** 1.0 (Rascunho Inicial)
- **Status:** Aprovado para Implementação

### 1.2 Resumo do Produto

**Family Locator (FamilySafe 360)** é uma plataforma robusta de monitoramento familiar projetada para oferecer tranquilidade através de rastreamento de localização em tempo real, alertas de segurança e análise de comportamento de direção. O sistema permite que grupos de confiança (famílias) criem "Círculos" para compartilhar localização ao vivo, receber notificações automáticas quando membros entram ou saem de lugares específicos (Geofencing) e acionar alertas de emergência (SOS).

O backend é arquitetado utilizando a **Arquitetura Vexa** (Spring-centric Hexagonal Architecture), alavancando um design orientado a eventos com Apache Kafka para garantir escalabilidade, resiliência e processamento em tempo real de alto volume de dados de localização.

---

## 2. Objetivos

### 2.1 Objetivos de Negócio

- **Escalabilidade:** Suportar ingestão de localização de alto throughput de milhares de dispositivos móveis concorrentes.
- **Conversão:** Impulsionar assinaturas premium oferecendo capacidades expandidas de Círculos.
- **Confiabilidade:** Garantir 99,9% de uptime para recursos críticos de segurança (SOS, Rastreamento de Localização).
- **Extensibilidade:** Construir um backend modular que permita fácil adição de novos consumidores (ex: analytics avançado) sem interromper serviços principais.

### 2.2 Objetivos do Usuário

- **Segurança:** Saber onde os membros da família estão em tempo real.
- **Consciência:** Ser notificado automaticamente quando um filho chega na escola ou um parceiro sai do trabalho (Geofencing).
- **Resposta a Emergências:** Notificar instantaneamente o círculo em caso de perigo via recurso SOS.
- **Privacidade:** Ter controle sobre quando a localização é compartilhada e garantir que os dados sejam armazenados de forma segura.

### 2.3 Não-Objetivos

- **Portal Web do Usuário:** Uma interface web para usuários finais visualizarem mapas está fora do escopo desta fase.
- **Recursos de Rede Social:** Sem feed, compartilhamento de fotos (exceto fotos de perfil) ou recursos de perfil público.
- **Algoritmos Complexos de Segurança:** Algoritmos detalhados de pontuação ou gamificação para segurança na direção são adiados; apenas detecção básica.
- **Suporte a Hardware Personalizado:** Integração com dongles OBD-II ou rastreadores personalizados; Apenas Aplicativo Móvel.

---

## 3. Personas de Usuário

### 3.1 Tipos Chave de Usuário

- **Administrador da Família (Pai/Mãe)**: O criador do círculo. Paga pela assinatura. Altamente preocupado com precisão e vida útil da bateria.
- **Membro da Família (Filho/Parceiro)**: Um membro do círculo. Precisa de uma experiência de aplicativo não intrusiva e ferramentas de emergência confiáveis.
- **Administrador do Sistema (Suporte)**: Equipe interna gerenciando a saúde da plataforma e resolvendo problemas de conta de usuário.

### 3.2 Detalhes Básicos da Persona

- **Ana (Administradora)**: 38 anos, mãe de dois filhos. Quer saber se seu filho chegou na escola em segurança sem ligar para ele todos os dias.
- **Léo (Membro)**: 14 anos. Quer fazer "check-in" facilmente e ter um botão de pânico para emergências.
- **Sam do Suporte (Interno)**: Precisa buscar usuários por e-mail, verificar status da assinatura e investigar atrasos na ingestão de localização.

### 3.3 Acesso Baseado em Função (RBAC)

- **CIRCLE_ADMIN**: Pode convidar membros, remover membros, editar configurações do círculo, gerenciar lugares/geofences.
- **CIRCLE_MEMBER**: Pode ver mapa, compartilhar localização, acionar SOS, chat e check-in.
- **SYS_ADMIN / SUPPORT**: Acesso ao Backoffice REST Ops.

---

## 4. Requisitos Funcionais

### 4.1 Domínio: Autenticação & Conta (Auth & Account)

- **Registro/Login**: Email/Senha e tokens de Login Social.
- **Gerenciamento de Sessão**: Fluxo de JWT Access Token + Refresh Token. Suporte para múltiplos dispositivos por usuário.
- **Perfil**: Gerenciar nome, foto de perfil e número de telefone.

### 4.2 Domínio: Círculos & Membros (Circles & Members)

- **Criar Círculo**: Usuários podem criar múltiplos círculos (Família, Amigos).
- **Fluxo de Convite**: Gerar códigos de convite alfanuméricos (com tempo limitado) ou deep links.
- **Fluxo de Entrada**: Validar código/link e adicionar usuário ao Círculo.
- **Limites de Membros (Aplicação do Plano)**:
  - **Plano Gratuito (Free)**: Máximo **5 membros** por círculo.
  - **Plano Premium**: Máximo **10 membros** por círculo.
- **Papéis**: Gerenciar permissões ADMIN e MEMBER dentro do círculo.

### 4.3 Domínio: Localização & Histórico (Location & History)

- **Ingestão de Stream**: Endpoint (`POST /locations/stream`) para aceitar pontos de localização em lote (lat, lon, precisão, timestamp, velocidade, bateria, atividade).
- **Processamento**: Publicar eventos brutos no Kafka `location.events`.
- **Última Localização Conhecida**: Atualizar cache Redis principalmente para busca de mapa em tempo real.
- **Retenção de Histórico**: Armazenar histórico detalhado de localização por **30 dias** para TODOS os usuários (Free & Premium).
- **Consulta de Histórico**: Endpoint para buscar caminho/pontos por intervalo de datas (hora início/fim).

### 4.4 Domínio: Lugares & Geofence (Places & Geofence)

- **Gerenciar Lugares**: Criar/Editar/Deletar zonas seguras (Coordenada central + Raio).
- **Detecção de Geofence**:
  - Consumidor orientado a eventos processa `location.events`.
  - Detectar eventos `ENTER` (Entrada) e `EXIT` (Saída) localmente usando funções PostGIS.
  - Publicar `geofence.events` no Kafka.

### 4.5 Domínio: Direção & Segurança (Driving & Safety)

- **Detecção de Viagem**:
  - Detectar automaticamente início e fim de viagens baseado em velocidade e flags de reconhecimento de atividade no stream de localização.
  - Criar entidades `Drive` com resumo (hora início, hora fim, velocidade máxima, distância).
- **Eventos de Risco**: Detecção básica de "Alta Velocidade", "Aceleração Rápida", "Frenagem Brusca" (baseado em metadados do stream).
- **Consumidor**: Processar `location.events` -> gerar `drive.events`.

### 4.6 Domínio: SOS & Incidentes (SOS & Incidents)

- **Botão de Pânico**: API para acionar um estado SOS imediato para um usuário.
- **Broadcast**:
  - Notificar imediatamente todos os membros do Círculo via Notificação Push e SMS (se configurado).
  - Payload de push de alta prioridade (alerta crítico).

### 4.7 Domínio: Chat & Check-in

- **Chat do Círculo**: Mensagens de texto simples em grupo por círculo.
- **Check-in Manual**: Botão "Estou aqui" envia um push para membros do círculo com o endereço/localização atual.

### 4.8 Domínio: Notificações (Notifications)

- **Canais**: Notificação Push (FCM/APNS), E-mail, SMS (apenas emergência).
- **Preferências**: Usuários podem alternar alertas por categoria (Alertas de Lugar, Alertas de Segurança, Chat).
- **Roteamento Inteligente**: `Notification Dispatch Consumer` escuta `geofence.events`, `drive.events`, `sos.events` para disparar envios.

### 4.9 Domínio: Faturamento & Planos (Billing & Plans)

- **Planos**:
  - **FREE**: Padrão. Limite 5 membros/círculo.
  - **PREMIUM**: Pago. Limite 10 membros/círculo.
- **Gerenciamento de Assinatura**: Validar recibos do Google Play / Apple App Store via webhook ou endpoint de verificação. Atualizar estado local da assinatura.

### 4.10 Domínio: Admin & Auditoria (Admin & Audit)

- **Busca de Usuário**: Por e-mail, ID ou telefone.
- **Log de Auditoria**: Rastrear ações críticas (Banir usuário, Alterar Plano manualmente) para conformidade.

---

## 5. Experiência do Usuário (Perspectiva App Mobile)

### 5.1 Pontos de Entrada

- **Onboarding**: Tela de Splash -> Autenticação (Cadastro/Login) -> Permissões (Localização Sempre, Notificação) -> Criar/Entrar em Círculo.

### 5.2 Experiência Principal

- **Visão de Mapa**: Posição em tempo real de todos os membros do círculo. Bottom sheet com lista de membros (status: movendo, parado, bateria %).
- **Linha do Tempo**: Slider/Calendário para visualizar histórico de localização de 30 dias.
- **Lugares**: Toque para adicionar "Casa", "Escola".
- **Segurança**: Ver lista de viagens recentes.

### 5.3 Fluxo Premium

- Se um usuário Free tentar convidar o 6º membro -> Mostrar paywall "Atualize para Premium".
- Fluxo de Compra In-App lida com a transação -> Backend atualiza estado -> Convite tem sucesso ou falha.

---

## 6. Métricas de Sucesso

### 6.1 Centradas no Usuário

- **Usuários Ativos Diários (DAU)**: Usuários enviando pelo menos uma atualização de localização.
- **Retenção**: Porcentagem de usuários ainda ativos após 30 dias.
- **Confiabilidade do SOS**: 100% de taxa de entrega de alertas SOS em < 10 segundos.

### 6.2 Métricas de Negócio

- **Taxa de Conversão**: % de usuários Free atualizando para Premium (atingindo o limite de 5 membros).
- **Taxa de Cancelamento (Churn)**: Assinaturas canceladas.

### 6.3 Métricas Técnicas

- **Latência de Ingestão**: Tempo de `POST /stream` até atualização no `Redis` (< 200ms).
- **Latência Ponta-a-Ponta**: Tempo do Evento de Localização até Notificação Push de Geofence (< 5s).

---

## 7. Considerações Técnicas

### 7.1 Arquitetura

- **Arquitetura Vexa**: Separação estrita de preocupações. `API` (Controllers) -> `Core` (Domínio/Serviços) <-> `Infrastructure` (Adaptadores).
- **Banco de Dados**: PostgreSQL para dados relacionais (Usuários, Círculos) + PostGIS para consultas espaciais (sobreposições de Geofence).
- **Barramento de Eventos**: Apache Kafka necessário para desacoplar ingestão de consumidores de processamento.

### 7.2 Privacidade de Dados & Retenção

- **Política de Retenção**: Um job agendado (Spring Batch ou Cron) deve rodar diariamente para fazer soft-delete ou arquivar dados de localização com mais de **30 dias**.
- **Segurança**: Todos os dados de localização em trânsito criptografados via TLS. Senhas com hash BCrypt.

### 7.3 Escalabilidade

- **Stream de Localização**: `LocationController` deve ser altamente performático, retornando `202 Accepted` imediatamente e delegando processamento para Kafka de forma assíncrona.
- **Consumidores**: Consumidores de Geofence e Detecção de Viagem devem ser idempotentes e escaláveis horizontalmente (grupos de consumidores Kafka).

---

## 8. Marcos e Sequenciamento

### 8.1 Fase 1: Fundação (Semanas 1-3)

- Estrutura base do projeto (estrutura Vexa).
- Design do banco de dados (PostgreSQL/PostGIS) e migrações Flyway.
- **Domínio Auth**: JWT, CRUD de Usuário.
- **Domínio Círculos**: Criar, Entrar (lógica básica).

### 8.2 Fase 2: Núcleo de Localização (Semanas 4-6)

- Integração Kafka (configuração Producer/Consumer).
- **Domínio Localização**: Endpoint de ingestão, atualização Redis.
- **Histórico**: Persistência no Postgres.

### 8.3 Fase 3: Inteligência & Notificações (Semanas 7-9)

- **Lugares & Geofence**: Lógica de detecção PostGIS.
- **Direção**: Algoritmos básicos de detecção de viagem.
- **Notificações**: Integração com FCM.

### 8.4 Fase 4: Monetização & Polimento (Semanas 10-12)

- **Faturamento**: Lógica de verificação da loja.
- **Limites Premium**: Aplicar a regra de 5 vs 10 membros.
- **Política de Retenção de 30 Dias**: Implementação dos jobs de limpeza.

---

## 9. Histórias de Usuário (Exemplo)

### 9.1 Rastreamento de Localização

- **ID**: LOC-001
- **Descrição**: Como usuário, quero que minha localização seja atualizada em segundo plano para que minha família saiba que estou seguro mesmo quando o aplicativo está fechado.
- **Critérios de Aceite**:
  - App envia atualizações em lote para `/locations/stream`.
  - Servidor aceita com 202.
  - Localização mais recente é visível no mapa dentro de 5 segundos.

### 9.2 Upgrade Premium

- **ID**: BIL-001
- **Descrição**: Como Administrador do Círculo, quero fazer upgrade para Premium para poder adicionar minha família estendida (mais de 5 pessoas).
- **Critérios de Aceite**:
  - Tentar convidar o 6º membro aciona uma verificação Premium.
  - Erro/Prompt retornado se o plano atual for FREE.
  - Após pagamento bem-sucedido, o limite aumenta para 10 imediatamente.

### 9.3 Alerta de Geofence

- **ID**: GEO-001
- **Descrição**: Como pai, quero receber uma notificação push quando meu filho sair da "Escola".
- **Critérios de Aceite**:
  - Definir raio da "Escola".
  - Receber notificação dentro de 1 minuto após a localização do filho sair do raio.
  - Histórico de notificações registra o evento.
