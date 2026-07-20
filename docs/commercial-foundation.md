# Commercial Module Foundation

## Objective

This phase introduces the first internal logistics-commercial foundation for FreightFlow:

- Incoterms 2020 catalog as code enum;
- tenant-scoped RFQs;
- cargo items and FCL container requirements;
- draft quotations;
- quotation items with deterministic financial totals;
- explicit RFQ and quotation state transitions;
- RBAC and multi-tenant isolation on the backend.

## Initial Scope

Functional scope in this phase is focused on ocean logistics:

- transport mode: `OCEAN`;
- directions: `IMPORT`, `EXPORT`;
- service types: `FCL`, `LCL`, `BREAK_BULK`.

Enums are already prepared for `AIR`, `ROAD`, `RAIL`, and `MULTIMODAL`, but no modal-specific workflow was added beyond baseline validation.

## RFQ

RFQ (`RequestForQuotation`) stores:

- customer or prospect;
- contact data;
- route with global ports;
- Incoterm 2020 code, version, and named place;
- cargo items;
- optional FCL containers;
- assigned internal user;
- tenant ownership.

### RFQ states

- `DRAFT`
- `SUBMITTED`
- `UNDER_ANALYSIS`
- `QUOTED`
- `CANCELLED`
- `EXPIRED`

### RFQ transitions implemented

- create as `DRAFT`
- update only in `DRAFT`
- `submit`
- `start-analysis`
- `cancel`

## Incoterms 2020

Supported:

- `EXW`
- `FCA`
- `CPT`
- `CIP`
- `DAP`
- `DPU`
- `DDP`
- `FAS`
- `FOB`
- `CFR`
- `CIF`

Rules:

- named place is mandatory when an Incoterm is informed;
- only version `2020` is accepted;
- `FAS`, `FOB`, `CFR`, and `CIF` require ocean context.

## Cargo and Containers

Each RFQ must include cargo items.

Validated rules include:

- positive package quantity and weight;
- positive volume when informed;
- dangerous goods require `unNumber`;
- temperature-controlled cargo requires min/max temperature;
- `minimumTemperature <= maximumTemperature`.

For containers:

- `FCL` requires at least one container;
- `LCL` and `BREAK_BULK` reject container rows in this phase;
- reefer consistency is checked for temperature-controlled FCL cargo.

## Quotation

Quotation is tenant-scoped and linked to one RFQ.

Implemented states:

- create as `DRAFT`
- update only in `DRAFT`
- add/update/delete items only in `DRAFT`
- mark as `READY_FOR_REVIEW` for internal review only
- cancel from draft/review states

Rules:

- quotation creation requires RFQ in `UNDER_ANALYSIS`
- RFQ in `SUBMITTED` must be moved explicitly to `UNDER_ANALYSIS` before quotation creation
- `READY_FOR_REVIEW` does not mark the RFQ as `QUOTED`
- `QUOTED` remains reserved for a future phase when the quotation is approved or sent to the customer

Future statuses already modeled:

- `APPROVED`
- `REJECTED`
- `SENT`
- `ACCEPTED`
- `DECLINED`
- `EXPIRED`

## Financial Rules

All money calculations run only on the backend using `BigDecimal`.

Conventions:

- money scale: `2`
- exchange-rate scale: `6`
- percentage scale: `4`
- rounding mode: `HALF_UP`

Formulas:

- `totalCost = costAmountInSellingCurrency × quantity`
- `totalSelling = sellingAmount × quantity`
- `profitAmount = totalSelling - totalCost`
- `marginPercentage = profitAmount / totalSelling × 100`
- `markupPercentage = profitAmount / totalCost × 100`

Exchange-rate semantics:

- `exchangeRate = units of sellingCurrency for 1 unit of costCurrency`
- `costAmountInSellingCurrency = costAmount × exchangeRate`
- example: `costCurrency=USD`, `sellingCurrency=BRL`, `costAmount=100`, `exchangeRate=5.25` => converted cost `525.00 BRL`
- when both currencies are equal, exchange rate may be omitted and the effective conversion is `1`

Special cases:

- zero selling total => margin `0`
- zero cost total => markup `0`
- optional items stay outside default quotation totals
- non-included items stay outside default quotation totals

## Multi-tenant and RBAC

Tenant isolation rules:

- every RFQ has `tenant_id`
- every quotation has `tenant_id`
- cross-tenant UUIDs behave as not found
- customer and assigned user must belong to the same tenant
- quotation creation validates the tenant-scoped RFQ

RBAC:

- `ADMIN`: full access within tenant
- `OPERATOR`: create/update/read RFQs and quotations
- `VIEWER`: read-only
- `CLIENT`: no access to the internal commercial module

## Out of Scope

Still outside this phase:

- frontend screens
- CRM funnel
- proposal PDF
- email send
- approval workflow
- booking conversion
- shipment conversion
- post-sale
- realized-cost comparison
- external FX integration
- carrier-rate integration

## Next Phase

Recommended next step:

- quotation revision cloning;
- internal approval workflow;
- customer-facing proposal delivery;
- explicit handoff from commercial to operations.
