package com.freightflow.modules.document;

/**
 * Tipos de documento suportados pelo módulo de gestão documental.
 *
 * <ul>
 *   <li>CTE   — Conhecimento de Transporte Eletrônico</li>
 *   <li>BL    — Bill of Lading (conhecimento de embarque marítimo)</li>
 *   <li>NF    — Nota Fiscal</li>
 *   <li>OTHER — Qualquer outro documento operacional ou comercial</li>
 * </ul>
 */
public enum DocumentType {
    CTE,
    BL,
    NF,
    OTHER
}
