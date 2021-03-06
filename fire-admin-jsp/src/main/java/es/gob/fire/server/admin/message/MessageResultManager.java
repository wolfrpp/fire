/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.admin.message;

/**
 * Manejador de resultados.
 */
public class MessageResultManager {


	private static final String OP_SELECC = "1"; //$NON-NLS-1$

	private static final String OP_ALTA = "2"; //$NON-NLS-1$

	private static final String OP_EDICION = "3"; //$NON-NLS-1$

	private static final String OP_BAJA = "4"; //$NON-NLS-1$

	private static final String OP_CLAVE = "clave";//$NON-NLS-1$

	private static final String RESULT_OK = "1"; //$NON-NLS-1$

	private static final String ENT_USR = "user";//$NON-NLS-1$

	private static final String ENT_APP = "app";//$NON-NLS-1$

	private static final String ENT_CER = "cer";//$NON-NLS-1$

	private static final String ENT_SRV = "srv"; //$NON-NLS-1$

	/**
	 * Analiza la respuesta de una operaci&oacute;n.
	 * @param op Identificador de la operaci&oacute;n.
	 * @param result Resultado de la operaci&oacute;n.
	 * @param entity Entidad sobre la que se realiza la operaci&oacute;n.
	 * @return Mensaje resultado.
	 */
	public static MessageResult analizeResponse(final String op, final String result, final String entity) {

		if (op == null || result == null || entity==null) {
			return null;
		}

		String msg = null;
		final boolean ok = RESULT_OK.equals(result);
		if (OP_ALTA.equals(op)) {
			if( entity.equals(ENT_APP)){
				msg = ok ?
						"La aplicaci&oacute;n ha sido dada de alta correctamente" : //$NON-NLS-1$
							"Error al dar de alta la aplicaci&oacute;n"; //$NON-NLS-1$
			}
			else if(entity.equals(ENT_USR)){
				msg = ok ?
						"El Usuario ha sido dado de alta correctamente" : //$NON-NLS-1$
							"Error al dar de alta al usuario"; //$NON-NLS-1$
			}
			else if(entity.equals(ENT_CER)){
				msg = ok ?
						"El Certificado ha sido dado de alta correctamente" : //$NON-NLS-1$
							"Error al dar de alta al Certificado"; //$NON-NLS-1$
			}
			else if(entity.equals(ENT_SRV)){
				msg = ok ?
						"El Servidor de logs ha sido dado de alta correctamente" : //$NON-NLS-1$
							"Error al dar de alta al servidor de logs"; //$NON-NLS-1$
			}

		}
		else if (OP_BAJA.equals(op)) {
			if( entity.equals(ENT_APP)){
				msg = ok ?
						"La aplicaci&oacute;n ha sido dada de baja correctamente" : //$NON-NLS-1$
						"Error al dar de baja la aplicaci&oacute;n"; //$NON-NLS-1$
			}
			else if(entity.equals(ENT_USR)){
				msg = ok ?
				"El Usuario ha sido dado de baja correctamente" : //$NON-NLS-1$
					"Error al dar de baja al usuario"; //$NON-NLS-1$
			}

			else if(entity.equals(ENT_CER)){
				msg = ok ?
				"El Certificado ha sido dado de baja correctamente" : //$NON-NLS-1$
					"Error al dar de baja al certificado"; //$NON-NLS-1$
			}
			else if(entity.equals(ENT_SRV)){
				msg = ok ?
				"El Servidor de logs ha sido dado de baja correctamente" : //$NON-NLS-1$
					"Error al dar de baja al Servidor de logs"; //$NON-NLS-1$
			}
		}
		else if (OP_EDICION.equals(op)) {
			if( entity.equals(ENT_APP)){
				msg = ok ?
						"La aplicaci&oacute;n ha sido editada correctamente" : //$NON-NLS-1$
						"Error al realizar la edicion de la aplicaci&oacute;n"; //$NON-NLS-1$
			}
			else if(entity.equals(ENT_USR)){
				msg = ok ?
						"El Usuario ha sido editado correctamente" : //$NON-NLS-1$
						"Error al realizar la edicion del usuario"; //$NON-NLS-1$
			}
			else if(entity.equals(ENT_CER)){
				msg = ok ?
						"El Certificado ha sido editado correctamente" : //$NON-NLS-1$
						"Error al realizar la edicion del certificado"; //$NON-NLS-1$
			}
			else if(entity.equals(ENT_SRV)){
				msg = ok ?
						"El Servidor de logs ha sido editado correctamente" : //$NON-NLS-1$
						"Error al realizar la edicion del Servidor de logs"; //$NON-NLS-1$
			}

		}
		else if(OP_CLAVE.equals(op)) {
			if(entity.equals(ENT_USR)){
				msg = ok ?
						"El Usuario ha cambiado su clave correctamente" : //$NON-NLS-1$
						"Error al cambiar la clave del usuario"; //$NON-NLS-1$
			}
		}
		else if (OP_SELECC.equals(op)) {
			 if(entity.equals(ENT_SRV)){
				msg = ok ?
				"Se ha conectado correctamente con el servidor de logs" : //$NON-NLS-1$
					"Error al conectar con el servidor de logs"; //$NON-NLS-1$
			}
		}
		else {
			return null;
		}

		return new MessageResult(ok, msg);
	}



}
