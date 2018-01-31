/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services.internal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.misc.Base64;
import es.gob.fire.server.connector.DocInfo;
import es.gob.fire.server.document.FIReDocumentManager;
import es.gob.fire.server.services.HttpCustomErrors;
import es.gob.fire.server.services.RequestParameters;
import es.gob.fire.server.services.ServiceUtil;
import es.gob.fire.server.services.document.DefaultFIReDocumentManager;
import es.gob.fire.signature.ConfigManager;

/**
 * Manejador que gestiona las peticiones para agregar nuevos documentos a un lote de firma.
 */
public class AddDocumentBatchManager {

	private static final Logger LOGGER = Logger.getLogger(AddDocumentBatchManager.class.getName());

    /**
     * Agrega un nuevo documento a un lote de firma.
	 * @param params Par&aacute;metros extra&iacute;dos de la petici&oacute;n.
	 * @param response Respuesta con el resultado de agregar el documento al lote.
	 * @throws IOException Cuando se produce un error de lectura o env&iacute;o de datos.
     */
    public static void addDocument(final RequestParameters params, final HttpServletResponse response)
    		throws IOException {

    	// Recogemos los parametros proporcionados en la peticion
    	final String appId			= params.getParameter(ServiceParams.HTTP_PARAM_APPLICATION_ID);
    	final String transactionId	= params.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
    	final String subjectId		= params.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);
    	final String docId			= params.getParameter(ServiceParams.HTTP_PARAM_DOCUMENT_ID);
    	final String dataB64		= params.getParameter(ServiceParams.HTTP_PARAM_DATA);

    	SignBatchConfig config;
    	try {
    		config = getParticularConfig(params);
    	}
    	catch (final IOException e) {
    		LOGGER.warning("Se han proporcionado parametros de configuracion de firma mal formados"); //$NON-NLS-1$
    		response.sendError(HttpServletResponse.SC_BAD_REQUEST,
    				"Se han proporcionado parametros de configuracion de firma mal formados"); //$NON-NLS-1$
    		return;
		}

    	final DocInfo docInfo = getDocInfo(params);

    	// Comprobamos que se hayan prorcionado los parametros indispensables
    	if (transactionId == null || transactionId.isEmpty()) {
    		LOGGER.warning("No se ha proporcionado el identificado de la transaccion"); //$NON-NLS-1$
    		response.sendError(HttpServletResponse.SC_BAD_REQUEST,
    				"No se ha proporcionado el identificador de la transaccion"); //$NON-NLS-1$
    		return;
    	}

    	LOGGER.fine(String.format("TrId %1s: AddDocumentBatchManager", transactionId)); //$NON-NLS-1$

    	final FireSession session = SessionCollector.getFireSession(transactionId, subjectId, null, false, true);
    	if (session == null) {
    		LOGGER.warning("La transaccion no se ha inicializado o ha caducado"); //$NON-NLS-1$
    		response.sendError(HttpCustomErrors.INVALID_TRANSACTION.getErrorCode());
    		return;
    	}

        // Si se definio un DocumentManager, lo usaremos para
        final FIReDocumentManager documentManager = (FIReDocumentManager) session.getObject(ServiceParams.SESSION_PARAM_DOCUMENT_MANAGER);
    	if (documentManager instanceof DefaultFIReDocumentManager && (dataB64 == null || dataB64.isEmpty())) {
    		LOGGER.warning("No se ha proporcionado el documento a firmar ni un gestor de documentos del que recuperarlo"); //$NON-NLS-1$
    		response.sendError(HttpServletResponse.SC_BAD_REQUEST,
    				"No se ha proporcionado el documento a firmar"); //$NON-NLS-1$
    		return;
    	}


    	final String format = config != null ? config.getFormat() :
    		session.getString(ServiceParams.SESSION_PARAM_FORMAT);
    	final Properties extraParams = config != null ? config.getExtraParams() :
    		ServiceUtil.base642Properties(session.getString(ServiceParams.SESSION_PARAM_EXTRA_PARAM));

    	byte[] docReferenceId;
        if (dataB64 != null && !dataB64.isEmpty()) {
        	try {
        		docReferenceId = Base64.decode(dataB64, true);
        	}
        	catch (final Exception e) {
        		LOGGER.warning("El documento enviado a firmar no esta bien codificado: " + e); //$NON-NLS-1$
        		response.sendError(HttpServletResponse.SC_BAD_REQUEST,
        				"El documento enviado a firmar no esta bien codificado"); //$NON-NLS-1$
        		return;
        	}
        }
        else {
        	docReferenceId = docId.getBytes(StandardCharsets.UTF_8);
        }

    	byte[] data;
    	try {
    		data = documentManager.getDocument(docReferenceId, appId, format, extraParams);
    	}
    	catch (final Exception e) {
    		LOGGER.log(Level.SEVERE, "Error en la carga de los datos a agregar al lote", e); //$NON-NLS-1$
    		response.sendError(HttpServletResponse.SC_BAD_REQUEST,
    				"Error en la carga de los datos a agregar al lote"); //$NON-NLS-1$
    		return;
    	}
    	if (data == null) {
    		LOGGER.warning("No se han podido obtener los datos para agregarlos al lote de firma"); //$NON-NLS-1$
    		response.sendError(HttpServletResponse.SC_BAD_REQUEST,
    				"No se han podido obtener los datos para agregarlos al lote de firma"); //$NON-NLS-1$
    		return;
    	}

    	// Recuperamos el listado de documentos del lote o lo creamos si este es el primero
        BatchResult batchResult = (BatchResult) session.getObject(ServiceParams.SESSION_PARAM_BATCH_RESULT);
        if (batchResult == null) {
        	batchResult = new BatchResult();
        }

        if (batchResult.hasDocument(docId)) {
        	LOGGER.warning("El identificador de documento indicado ya existe en el lote"); //$NON-NLS-1$
        	response.sendError(HttpCustomErrors.DUPLICATE_DOCUMENT.getErrorCode());
        	return;
        }

        // Almacenamos el documento en disco, registramos su ID
        //final int docNumber = ((Integer) configOperation.getOrDefault(ServiceParams.SESSION_PARAM_BATCH_NUM_DOCS, Integer.valueOf(0))).intValue() + 1;
        final int maxDocuments = ConfigManager.getBatchMaxDocuments();
        if (maxDocuments != ConfigManager.UNLIMITED_NUM_DOCUMENTS && batchResult.documentsCount() >= maxDocuments) {
        	LOGGER.warning("Se ha excedido el numero maximo de documentos permitido en el lote"); //$NON-NLS-1$
        	response.sendError(HttpCustomErrors.NUM_DOCUMENTS_EXCEEDED.getErrorCode());
        	return;
        }

        final String filename = transactionId + "_" + docId; //$NON-NLS-1$

        try {
        	TempFilesHelper.storeTempData(filename, data);
        }
        catch (final Exception e) {
        	LOGGER.severe("Error en el guardado temporal de los datos a firmar: " + e); //$NON-NLS-1$
        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        	return;
        }

        // Si se establecio una configuracion particular para esta firma,
        // introducimos el formato de actualizacion, como parte de los
        // extraParams para que el Cliente @firma (que no soporta la actualizacion
        // en los lotes) lo pueda identificar
        if (config != null) {
        	final String upgrade = config.getUpgrade();
        	if (upgrade == null) {
        		if (config.getExtraParams() != null) {
        			config.getExtraParams().remove(MiniAppletHelper.PARAM_CONFIG_UPGRADE);
        		}
        	}
        	else {
        		if (config.getExtraParams() == null) {
        			config.setExtraParamsB64(""); //$NON-NLS-1$
        		}
        		config.getExtraParams().setProperty(MiniAppletHelper.PARAM_CONFIG_UPGRADE, config.getUpgrade());
        	}
        }

        batchResult.addDocument(docId, filename, config, docInfo);

        session.setAttribute(ServiceParams.SESSION_PARAM_BATCH_RESULT, batchResult);
        SessionCollector.commit(session);

        // Devolvemos el resultado de la operacion
        response.getWriter().print(Boolean.TRUE.toString());
    }

	/**
     * Extrae de la peticion la configuraci&oacute;n de firma que se debe
     * aplicar a este documento particular.
     * @param request Solicitud de agregar documento al lote.
     * @return Configuraci&oacute;n de firma particular o {@code null} si no
     * se defini&oacute;.
     * @throws IOException Si se configuran extraParams y no son un Base 64 bien formado.
     */
	private static SignBatchConfig getParticularConfig(final RequestParameters params) throws IOException {

		// Comprobamos si se ha establecido configuracion particular
		if (!params.containsKey(ServiceParams.HTTP_PARAM_CRYPTO_OPERATION) &&
				!params.containsKey(ServiceParams.HTTP_PARAM_FORMAT) &&
				!params.containsKey(ServiceParams.HTTP_PARAM_EXTRA_PARAM) &&
				!params.containsKey(ServiceParams.HTTP_PARAM_UPGRADE)) {
			return null;
		}

		final SignBatchConfig config = new SignBatchConfig();
		config.setCryptoOperation(params.getParameter(ServiceParams.HTTP_PARAM_CRYPTO_OPERATION));
		config.setFormat(params.getParameter(ServiceParams.HTTP_PARAM_FORMAT));
		config.setExtraParamsB64(params.getParameter(ServiceParams.HTTP_PARAM_EXTRA_PARAM));
		config.setUpgrade(params.getParameter(ServiceParams.HTTP_PARAM_UPGRADE));

		return config;
	}

	/**
     * Extrae de la petici&oacute;n informaci&oacute;n del documento.
     * @param request Solicitud de agregar documento al lote.
     * @return Informaci&oaucte;n del documento enviada en la petici&oacute;n
     * o {@code null} si no se env&iacute;a.
	 * @throws IOException Si la configuraci&oacute;n con la informaci&oacute;n
	 * del documento no est&aacute; bien codificada.
     */
	private static DocInfo getDocInfo(final RequestParameters params) throws IOException {

		// Comprobamos si se ha establecido configuracion particular
		if (!params.containsKey(ServiceParams.HTTP_PARAM_CONFIG)) {
			return null;
		}

		final Properties config = AOUtil.base642Properties(params.getParameter(ServiceParams.HTTP_PARAM_CONFIG));
		return DocInfo.extractDocInfo(config);
	}
}
