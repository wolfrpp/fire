/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.signature;


/**
 * Excepci&oacute;n de operaci&oacute;n inv&aacute;lida en una petici&oacute;n a
 * un servicio.
 */
public final class FIReInvalidRequestOperationException extends
	FIReInvalidRequestException {

    private static final long serialVersionUID = 392897533326297380L;

    /**
     * Construye una excepci&oacute;n de operaci&oacute;n inv&aacute;lida en una
     * petici&oacute;n a un servicio.
     *
     * @param cause
     *            Mensaje de la excepci&oacute;n.
     */
    public FIReInvalidRequestOperationException(final String cause) {
        super(cause);
    }

}
