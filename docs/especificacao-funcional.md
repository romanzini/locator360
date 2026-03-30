# Especificação Funcional

## 1. Visão Geral

- **Nome do produto:** Locator 360
- **Objetivo:** Aplicativo móvel para monitoramento de localização em tempo real de membros de grupos familiares, com foco em segurança pessoal, direção segura e alertas de emergência.
- **Plataformas alvo:** Android, iOS (backend em nuvem com painel web opcional).
- **Público principal:** Famílias, casais, responsáveis por idosos e crianças, pequenos grupos de confiança.

---

## 2. Conceitos Centrais

- **Usuário:** Pessoa cadastrada no sistema, autenticada por e-mail/telefone/sso.
- **Círculo:** Grupo privado de usuários (ex.: “Família”, “Amigos Próximos”).
- **Lugar (Place):** Local de interesse cadastrado (casa, escola, trabalho) com geofence.
- **Sessão de Localização:** Período em que o app está ativo e enviando localização.
- **Evento de Localização:** Registro de posição (lat, long, data/hora, velocidade etc.).
- **Evento de Direção:** Trecho de deslocamento (início/fim) com dados de condução.
- **Alerta:** Notificação gerada pelo sistema (entrada/saída de lugar, SOS, bateria etc.).

---

## 3. Personas

- **Pai/Mãe Responsável**
  - Deseja saber onde os filhos estão em tempo real.
  - Quer receber alertas de chegada/saída da escola, casa, curso.
  - Prefere relatórios simples de direção (velocidade, frenagens, uso do celular).

- **Filho/Adolescente**
  - Quer compartilhar apenas com pessoas autorizadas.
  - Deseja privacidade controlada (pausar compartilhamento em certos momentos).
  - Pode usar SOS em situações de emergência.

- **Idoso/Responsável por Idoso**
  - Necessita de monitoramento discreto (quedas, imobilidade, rotas habituais).
  - Família deseja ser alertada em situações anômalas ou emergenciais.

---

## 4. Módulo de Conta & Autenticação

### 4.1 Cadastro de Usuário

**Objetivo:** Permitir o cadastro simples e seguro.

**Requisitos Funcionais:**

- RF-001 – Permitir cadastro via:
  - E-mail + senha
  - Telefone + código SMS
  - Login social (Google, Apple, Facebook) – opcional.
- RF-002 – Validar e-mail/telefone com código de verificação.
- RF-003 – Armazenar dados básicos do perfil:
  - Nome
  - Sobrenome
  - Foto de perfil
  - Sexo (opcional)
  - Data de nascimento (opcional)
- RF-004 – Permitir redefinição de senha via e-mail/SMS.

### 4.2 Login & Sessão

- RF-005 – Permitir login com múltiplos métodos (conforme cadastro).
- RF-006 – Manter sessão persistente (token) no dispositivo.
- RF-007 – Permitir logout manual e revogação de sessões em outros dispositivos.

### 4.3 Perfil do Usuário

- RF-008 – Permitir edição de dados de perfil.
- RF-009 – Permitir configuração de:
  - Idioma
  - Fuso horário
  - Unidades de medida (km/h, milhas/h).
- RF-010 – Exibir status de assinatura (plano gratuito/premium).

### 4.4 Identidades, Verificação e Dispositivos

- RF-011A – Permitir vincular múltiplas identidades de autenticação para o mesmo usuário (senha, social, telefone).
- RF-011B – Registrar e controlar tokens de verificação/redefinição com expiração e uso único.
- RF-011C – Permitir gerenciar dispositivos ativos do usuário para sessão e push notification.
- RF-011D – Permitir revogar sessão em dispositivo específico sem afetar os demais.

---

## 5. Módulo de Círculos (Grupos)

### 5.1 Criação e Gestão de Círculos

**Objetivo:** Organizar usuários em grupos privados.

**Requisitos Funcionais:**

- RF-011 – Permitir criar múltiplos círculos por usuário (ex.: “Família”, “Trabalho”).
- RF-012 – Definir atributos de círculo:
  - Nome
  - Foto/ícone
  - Cor de identificação.
- RF-013 – Usuário criador é “Administrador” do círculo por padrão.

### 5.2 Convite e Entrada em Círculos

- RF-014 – Gerar código de convite ou link compartilhável.
- RF-015 – Permitir entrada no círculo via:
  - Código de convite
  - Link de convite
  - Convite explícito enviado para e-mail/telefone.
- RF-016 – Exigir aceite do convite pelo usuário convidado.- RF-016A – Após o usuário convidado entrar no círculo, notificar todos os demais membros ativos do círculo com uma notificação do tipo `MEMBER_JOINED` contendo o nome do novo membro.- RF-017 – Administrador pode aprovar/rejeitar novas entradas se círculo for “privado”.

### 5.3 Papéis e Permissões em Círculos

- RF-018 – Definir papéis:
  - Administrador: gerencia membros, lugares, configurações.
  - Membro: compartilha localização e visualiza demais membros.
- RF-019 – Permitir ao administrador:
  - Remover membros do círculo.
  - Transferir administração para outro membro.
  - Bloquear novo ingresso de membros (círculo fechado).
- RF-019A – Após remover um membro, notificar todos os demais membros ativos do círculo com uma notificação do tipo `MEMBER_REMOVED` contendo o nome do membro removido.
- RF-019B – Após transferir a administração, notificar todos os membros do círculo com uma notificação do tipo `ADMIN_TRANSFERRED` contendo o nome do novo administrador.

### 5.4 Sair e Excluir Círculo

- RF-020 – Permitir ao usuário sair de um círculo.
- RF-020A – Após o usuário sair do círculo, notificar todos os membros restantes do círculo com uma notificação do tipo `MEMBER_LEFT` contendo o nome do membro que saiu.
- RF-021 – Se o único administrador sair:
  - Transferir automaticamente a administração para outro membro ou
  - Notificar antes para que escolha um substituto.
- RF-022 – Permitir que o último administrador exclua o círculo (remoção lógica).

---

## 6. Módulo de Localização em Tempo Real

### 6.1 Coleta de Localização

**Objetivo:** Capturar e enviar a posição dos usuários em tempo quase real, com economia de bateria.

**Requisitos Funcionais:**

- RF-023 – Obter localização via GPS, Wi-Fi e rede móvel.
- RF-024 – Ajustar frequência de atualização de acordo com:
  - Modo de energia (normal, economia).
  - Movimento do usuário (parado, andando, dirigindo).
- RF-025 – Armazenar eventos de localização quando offline e sincronizar assim que houver conexão.

### 6.2 Compartilhamento de Localização

- RF-026 – Compartilhar posição atual com todos os membros do círculo (por padrão).
- RF-027 – Permitir ao usuário:
  - Pausar o compartilhamento de localização.
  - Retomar o compartilhamento.
- RF-028 – Indicar visualmente no mapa:
  - Usuários online (localização recente).
  - Usuários com localização desatualizada.
  - Usuários com compartilhamento pausado.

### 6.3 Mapa e Visualização

- RF-029 – Exibir mapa com:
  - Ícone/foto de cada membro.
  - Última posição conhecida.
  - Direção de movimento (opcional).
- RF-030 – Permitir zoom e arrastar mapa.
- RF-031 – Exibir status adicional no ícone:
  - Nível de bateria do dispositivo.
  - Tipo de transporte estimado (a pé, carro) – opcional.
- RF-032 – Permitir seleção de um membro para:
  - Ver detalhes de localização (endereço aproximado).
  - Ver horário da última atualização.

---

## 7. Módulo de Lugares (Places) & Cercas Virtuais

### 7.1 Cadastro de Lugares

**Objetivo:** Definir locais de interesse para gerar alertas de entrada/saída.

**Requisitos Funcionais:**

- RF-033 – Permitir cadastrar lugares por círculo:
  - Casa
  - Escola
  - Trabalho
  - Lugares personalizados.
- RF-034 – Definir atributos:
  - Nome
  - Endereço (autocomplete por mapa)
  - Coordenadas e raio (geofence).
- RF-035 – Permitir editar e excluir lugares.

### 7.2 Alertas de Entrada/Saída

- RF-036 – Detectar automaticamente quando um membro:
  - Entra na área do lugar (geofence).
  - Sai da área do lugar.
- RF-037 – Gerar alertas para:
  - Todos os membros do círculo ou
  - Apenas responsáveis configurados.
- RF-038 – Permitir configurar:
  - Tipo de alerta (push, e-mail – opcional).
  - Horários em que o alerta é relevante (ex.: só dias úteis).

---

## 8. Módulo de Histórico de Localização

### 8.1 Linha do Tempo

**Objetivo:** Permitir visualizar onde cada membro esteve em determinado período.

**Requisitos Funcionais:**

- RF-039 – Armazenar histórico de localização por usuário.
- RF-040 – Permitir filtro por:
  - Dia
  - Intervalo de datas
  - Usuário específico.
- RF-041 – Exibir no mapa o trajeto percorrido em um dia.
- RF-042 – Exibir lista de eventos:
  - Horário
  - Posição aproximada (endereço).
- RF-043 – Respeitar políticas de retenção por plano:
  - Gratuito: poucos dias de histórico.
  - Premium: meses ou ano de histórico.

---

## 9. Módulo de Direção & Segurança no Trânsito

Escopo desta versão: detecção e monitoramento de direção limitados a viagens de carro (`mode = CAR`).

### 9.1 Detecção de Viagens (Drives)

**Objetivo:** Identificar automaticamente quando um usuário está dirigindo e registrar dados de direção.

**Requisitos Funcionais:**

- RF-044 – Detectar início de viagem ao identificar:
  - Velocidade > limiar (ex.: 20 km/h) estável por X segundos.
- RF-045 – Detectar fim de viagem após:
  - Período de baixa velocidade/parado por Y minutos.
- RF-046 – Registrar para cada viagem:
  - Início (data/hora, localização).
  - Fim (data/hora, localização).
  - Distância aproximada.
  - Duração.
  - Velocidade média e máxima.

### 9.2 Comportamento de Direção

- RF-047 – Detectar eventos de:
  - Excesso de velocidade (acima de limite configurado).
  - Frenagens bruscas.
  - Acelerações bruscas.
  - Curvas bruscas.
- RF-048 – Classificar cada viagem com uma nota de segurança (ex.: 0–100).
- RF-049 – Gerar relatório de direção por usuário:
  - Por viagem
  - Por dia/semana/mês (agregado).

### 9.3 Alertas de Direção

- RF-050 – Notificar responsáveis quando:
  - Um menor de idade inicia/termina uma viagem.
  - Há evento grave (alta velocidade extrema).
- RF-051 – Permitir desligar/ajustar nível de sensibilidade de alertas por círculo.

---

## 10. Módulo de SOS & Emergência

### 10.1 Botão de SOS

**Objetivo:** Fornecer ao usuário um mecanismo rápido de pedido de ajuda.

**Requisitos Funcionais:**

- RF-052 – Exibir botão de SOS na tela principal.
- RF-053 – Ao acionar SOS:
  - Mostrar contagem regressiva (ex.: 5 segundos) para cancelar.
  - Enviar alerta de emergência apenas para os membros configurados para receber SOS (opcional).
- RF-054 – Alertar via:
  - Push notificação
  - SMS (opcional, dependente de infraestrutura)
  - E-mail (opcional).

  #### Ajustar todas as informações para que esta funcionalidade possa ser implementada, por exemplo

  - Buscar os dados de gerar token e depois buscar as informações para enviar para a URA fazer a ligação para o número de emergência
  
### 10.2 Conteúdo do Alerta de SOS

- RF-055 – Incluir no alerta:
  - Nome do usuário.
  - Círculo de origem.
  - Localização atual (link para mapa).
  - Data e hora do disparo.
- RF-056 – Permitir que o usuário marque a emergência como “Resolvida”.

---

## 11. Módulo de Detecção de Incidentes/Acidentes (Opcional – Avançado)

### 11.1 Detecção de Colisão

**Objetivo:** Detectar automaticamente possíveis acidentes graves de trânsito.

**Requisitos Funcionais:**

- RF-057 – Utilizar acelerômetro, giroscópio e padrões de desaceleração brusca para identificar possível colisão.
- RF-058 – Em caso suspeito de colisão:
  - Ativar fluxo similar ao SOS automático.
  - Disparar notificação para membros do círculo.
- RF-059 – Permitir cancelamento manual rápido (para falsos positivos).

### 11.2 Integração com Serviços de Emergência (Escopo Futuro)

- RF-060 – Prever integração futura com números de emergência locais (ex.: discagem rápida, envio pré-formatado – respeitando regulamentações por país).

---

## 12. Módulo de Mensagens & Check-ins

### 12.1 Chat entre Membros

**Objetivo:** Facilitar comunicação entre membros do círculo.

**Requisitos Funcionais:**

- RF-061 – Oferecer chat de grupo por círculo.
- RF-062 – Permitir mensagens de texto.
- RF-063 – Exibir indicação de leitura (lido/entregue) – opcional.
- RF-064 – Exibir quem está online recentemente (se permitido).

### 12.2 Check-in Manual

- RF-065 – Permitir que o usuário faça um “check-in” em sua localização atual:
  - Mensagem curta (ex.: “Cheguei bem em casa”).
  - Localização anexada.
- RF-066 – Enviar notificação de check-in para o círculo selecionado.

---

## 13. Módulo de Notificações

### 13.1 Tipos de Notificações

**Requisitos Funcionais:**

- RF-067 – Entradas/saídas de lugares.
- RF-068 – Início/término de viagens.
- RF-069 – Eventos de direção arriscada.
- RF-070 – Alertas de SOS/emergência.
- RF-071 – Bateria fraca do dispositivo de um membro.
- RF-072 – Convites e alterações em círculos.

### 13.2 Preferências de Notificação

- RF-073 – Permitir personalizar por usuário:
  - Quais tipos de alerta receber.
  - Intensidade (som, vibração, silencioso).
- RF-074 – Permitir silenciar temporariamente notificações de um círculo.

---

## 14. Módulo de Assinaturas & Planos

### 14.1 Plano Gratuito

**Funcionalidades típicas:**

- RF-075 – Limite de número de círculos.
- RF-076 – Limite de número de lugares.
- RF-077 – Histórico de localização reduzido (poucos dias).
- RF-078 – Funções básicas de localização em tempo real e notificações.

### 14.2 Plano Premium

- RF-079 – Histórico de localização estendido.
- RF-080 – Mais círculos e lugares por círculo.
- RF-081 – Relatórios avançados de direção.
- RF-082 – Detecção de incidentes (se implementada).
- RF-083 – Suporte prioritário (se houver).

### 14.3 Gestão de Assinaturas

- RF-084 – Integração com lojas nativas (Google Play, App Store).
- RF-085 – Exibir status de assinatura, data de renovação e forma de pagamento.
- RF-086 – Permitir cancelamento/upgrade/downgrade conforme políticas das lojas.

---

## 15. Módulo de Privacidade & Segurança

### 15.1 Controles de Privacidade

**Requisitos Funcionais:**

- RF-087 – Permitir pausar o compartilhamento de localização (por tempo indeterminado ou intervalo definido).
- RF-088 – Permitir escolher:
  - Em quais círculos compartilhar a localização.
  - Em quais círculos exibir histórico.
- RF-089 – Exibir claramente quando a localização está pausada para o próprio usuário.

### 15.2 Transparência e Logs

- RF-090 – Exibir ao usuário:
  - Quem tem acesso à sua localização (quais círculos, quais membros).
- RF-091 – Opcional: permitir registrar e exibir histórico de acessos (consultas à localização).

### 15.3 Proteção de Dados

- RF-092 – Criptografar comunicações entre app e servidor (HTTPS/TLS).
- RF-093 – Armazenar senhas com hash seguro.
- RF-094 – Aplicar regras de anonimização/mascaramento em relatórios agregados.

---

## 16. Módulo Administrativo (Backend / Painel Web Interno)

### 16.1 Gestão de Usuários

**Requisitos Funcionais:**

- RF-095 – Permitir a administradores internos do sistema:
  - Buscar usuários por e-mail/telefone/ID.
  - Verificar status de conta, círculos associados e plano.
- RF-096 – Bloquear conta em caso de abuso ou uso indevido.

### 16.2 Monitoramento & Suporte

- RF-097 – Visualizar logs de erros, desempenho de serviços e fila de notificações.
- RF-098 – Ferramentas para suporte:
  - Reset de tokens
  - Forçar logout global
  - Ajustes de plano/benefícios.

### 16.3 Governança e Auditoria Administrativa

- RF-099 – Permitir registrar marcações administrativas em usuários (ex.: abuso, fraude suspeita, outros).
- RF-100 – Permitir consultar trilha de auditoria de ações sensíveis do backoffice.
- RF-101 – Garantir controle de acesso por papéis administrativos (`SUPPORT`, `ADMIN`, `SUPER_ADMIN`).

---

## 17. Requisitos Não Funcionais (Resumo)

- RNF-001 – Disponibilidade alvo do backend: ≥ 99,5%.
- RNF-002 – Latência média para atualização de localização: < 10s em condições normais.
- RNF-003 – Uso eficiente de bateria no app:
  - Otimização de intervalos de GPS.
  - Ajuste de precisão quando em segundo plano.
- RNF-004 – Escalabilidade horizontal do backend para suportar grande número de usuários.
- RNF-005 – Conformidade com legislações de privacidade aplicáveis (ex.: LGPD/GDPR).
- RNF-006 – Localização da interface em múltiplos idiomas (inicialmente PT-BR, EN).

---

## 18. Backlog Futuro (Ideias)

- RF-FUT-001 – Integração com dispositivos wearables (smartwatch).
- RF-FUT-002 – Detecção de quedas para idosos.
- RF-FUT-003 – Modos especiais (ex.: “Caminho para casa” com monitoramento mais intenso).
- RF-FUT-004 – Estatísticas gamificadas de direção segura (pontuação entre membros do círculo).

---

## 19. Rastreabilidade com Modelo de Dados

Para garantir aderência entre requisito funcional e persistência:

- Conta e autenticação: `users`, `auth_identities`, `verification_tokens`, `devices`.
- Círculos e membros: `circles`, `circle_members`, `circle_invites`, `circle_settings`.
- Localização e privacidade por círculo: `locations`, `location_sharing_states`.
- Lugares e geofences: `places`, `place_alert_policies`, `place_alert_targets`, `place_events`.
- Direção e segurança: `drives`, `drive_events`.
- SOS e incidentes: `sos_events`, `incident_detections`.
- Comunicação: `circle_messages`, `circle_message_receipts`, `checkins`.
- Notificações: `notification_preferences`, `notifications`.
- Planos e assinatura: `plans`, `subscriptions`.
- Backoffice: `admin_users`, `user_flags`, `audit_logs`.

---
