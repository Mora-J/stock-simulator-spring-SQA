# Informe de Pruebas Unitarias — Adecuación Funcional (ISO/IEC 25010)

Implementación de los 13 casos de prueba (CP-01 a CP-13) especificados en el informe de QA del backend de Stock Simulator. Cada caso vive en su propia clase JUnit 5 + Mockito, en el mismo paquete que la clase bajo prueba, sin tocar la base de datos real.

## Cómo ejecutar

El entorno de desarrollo tiene un problema de certificado SSL que bloquea la resolución normal contra Maven Central (`PKIX path building failed`). Mientras no se resuelva, usar el modo offline del wrapper de Maven:

```powershell
.\mvnw.cmd -o test
```

Para correr una sola clase:

```powershell
.\mvnw.cmd -o test "-Dtest=NombreDeLaClaseTest"
```

## Resultado global

| | |
|---|---|
| Clases que compilan | 13 / 13 |
| Tests que pasan | 5 |
| Tests que fallan (documentan bugs, fallo esperado) | 12 |
| Casos no implementados | 0 |

## ✅ Exitosas (PASS)

| Caso | Clase | Test | Qué confirma |
|------|-------|------|---------------|
| CP-03 | `TransactionServiceVerifyVisaTest` | `verifyVISAWithEmptyCardShouldReturnFalse` | Una tarjeta vacía se rechaza sin excepción |
| CP-04 | `UserServiceRegisterPasswordHashTest` | `passwordShouldBeHashedBeforePersisting` | El password se hashea con BCrypt antes de `save()` |
| CP-11 | `StockControllerResponseWrapperTest` | `availableStocksShouldBeWrappedInStandardResponseDTO` | La lista de acciones viene envuelta en `StockListResponseDTO` |
| CP-13 | `PasswordUtilMatchesTest` | `matchesReturnsTrueForCorrectPassword` | `matches()` reconoce el password correcto |
| CP-13 | `PasswordUtilMatchesTest` | `matchesReturnsFalseWhenPasswordIsAltered` | `matches()` rechaza un password alterado |

## ❌ Fallidas (fallo esperado — documentan bugs reales)

| Caso | Clase | Test | Resultado real vs esperado |
|------|-------|------|------------------------------|
| CP-01 | `BuyRequestDTOTest` | `negativeQuantityAndEmptyTickerShouldBeRejectedByValidator` | 0 violaciones (esperaba ≥1) — `BuyRequestDTO` no tiene anotaciones de validación |
| CP-01 | `BuyRequestDTOTest` | `zeroQuantityShouldBeRejectedByValidator` | 0 violaciones (esperaba ≥1) |
| CP-02 | `SellRequestDTOTest` | `nullTickerShouldBeRejectedAsMissingRequiredField` | 0 violaciones (esperaba ≥1) — `SellRequestDTO` no tiene anotaciones de validación |
| CP-03 | `TransactionServiceVerifyVisaTest` | `verifyVISAWithNullCardShouldReturnFalseInsteadOfThrowing` | Lanza `NullPointerException` (esperaba `false`) |
| CP-05 | `TransactionAmountRoundingTest` | `floatAmountAccumulatesBinaryRoundingError` | `15.110001` (esperaba `15.11` exacto) — `Transaction.amount` es `Float` |
| CP-06 | `OwnedStockNegativeBalanceTest` | `reducingQuantityBelowZeroShouldBeRejectedByEntity` | `quantity=-10` (esperaba ≥0) — `OwnedStock.setQuantity` no valida |
| CP-07 | `StockEODServiceTimeoutTest` | `timeoutShouldRaiseControlledExceptionInsteadOfReturningNull` | No lanza nada, retorna `null` ante timeout |
| CP-08 | `StockServiceMapToDTOTest` | `mappingShouldPreserveAllEntityFieldsInDTO` | Falta el campo `id` en `StockDTO` — se pierde en el mapeo |
| CP-09 | `TransactionControllerInsufficientBalanceTest` | `sellingMoreSharesThanOwnedShouldReturnBadRequestAndNeverPersist` | Responde `200 OK` (esperaba `400`) e invoca los repos igual |
| CP-10 | `UserControllerDuplicateUsernameTest` | `registeringExistingUsernameShouldReturnConflict` | Responde `200 OK` (esperaba `409 Conflict`) |
| CP-12 | `SecurityConfigurationJwtTest` | `transactionalRouteWithoutJwtShouldBeRejected` | Responde `200 OK` sin token (esperaba `401`/`403`) — no hay filtro JWT |

## Notas

- **No incluido en este informe:** `StockSimulatorApplicationTests.contextLoads`, test preexistente no relacionado con estos 13 casos, que ya fallaba antes de este trabajo por falta de conexión a PostgreSQL real en el entorno.
- **Cambios de infraestructura en `pom.xml`** (no de lógica de producción): se agregaron `jakarta.validation-api` y `hibernate-validator` (scope `test`, necesarios para CP-01/CP-02 porque el proyecto no declara `spring-boot-starter-validation`), se fijó `maven-surefire-plugin:3.5.4` y se agregó `junit-platform-launcher`, para poder compilar y correr todo en modo offline dado el problema de certificado SSL del entorno.
- Ningún test de los que documentan bugs fue "arreglado" modificando el código de producción — el fallo es intencional y debe persistir hasta que el equipo corrija el defecto correspondiente.
