package com.xibodoh.smartcruiserrc.httpd;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import android.content.res.AssetManager;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response;

public class AssetHandler extends FileHandler {

	private final AssetManager assetManager;

	public AssetHandler(AssetManager assetManager) {
		super(null);
		this.assetManager = assetManager;

	}

	@Override
	protected Response respond(Map<String, String> header, String uri) {
		Response res = null;
        
		if ( uri.startsWith("/") ) {
            uri = uri.substring(1, uri.length());
        }
		
		if (uri.endsWith("/") || uri.equalsIgnoreCase("")) {
			uri = uri + "index.html";
		}

		InputStream assetFile = null;
		try {
			assetFile = assetManager.open(uri);
		} catch (IOException ex) {
			assetFile = null;
		}
		if (assetFile == null) {
			return createResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Error 404, file not found.");
		}


		// Get MIME type from file name extension, if possible
		String mime = getMimeTypeForFile(uri);

		// Calculate etag

		try {
			String etag = Integer.toHexString((uri + "" + assetFile.available()).hashCode());
			res = serveStream(uri, header, assetFile, assetFile.available(), mime, etag);
		} catch (IOException ioe) {
			res = createResponse(Response.Status.FORBIDDEN,	NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: Reading file failed.");
		}
		return res;
	}

}
