# Notas sobre Migração do Room Database

## Problema Resolvido
O app estava crashando ao iniciar devido a incompatibilidade entre o schema antigo do Room e o banco local existente no dispositivo/emulador.

## Solução Aplicada
1. **Versão do banco incrementada**: De 4 para 5
2. **fallbackToDestructiveMigration() adicionado**: Permite que o Room destrua e recrie o banco automaticamente se houver problemas de migração
3. **Migrations mantidas**: As migrations 1→2, 2→3 e 3→4 foram mantidas para referência futura

## Importante
- Durante desenvolvimento, é seguro usar `fallbackToDestructiveMigration()` pois permite perder dados de teste
- Em produção, seria necessário criar migrations adequadas para preservar dados dos usuários
- O banco será recriado automaticamente na primeira execução após essas mudanças

## Estrutura Atual do Banco
- **Tabela `cows`**: Vacas com campo opcional `dataInicioPrenhez`
- **Tabela `eventos`**: Eventos sem `cowId` (removido na versão 4)
- **Tabela `event_cow_cross_ref`**: Relação N:N entre eventos e vacas
- **Tabela `financial_transactions`**: Transações financeiras

