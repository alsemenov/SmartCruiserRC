package com.xibodoh.smartcruiserrc.httpd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.xibodoh.smartcruiserrc.httpd.HTTPServer.RequestHandler;

import android.content.res.AssetManager;


import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;

public class FileHandler implements RequestHandler {

    /**
     * Common mime type for dynamic content: binary
     */
    public static final String MIME_DEFAULT_BINARY = "application/octet-stream";
    /**
     * Default Index file names.
     */
    public static final List<String> INDEX_FILE_NAMES = new ArrayList<String>() {{
        add("index.html");
        add("index.htm");
    }};
	
    /**
     * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
     */
    public static final Map<String, String> MIME_TYPES = new HashMap<String, String>() {{
        put("css", "text/css");
        put("htm", "text/html");
        put("html", "text/html");
        put("xml", "text/xml");
        put("java", "text/x-java-source, text/java");
        put("md", "text/plain");
        put("txt", "text/plain");
        put("asc", "text/plain");
        put("gif", "image/gif");
        put("jpg", "image/jpeg");
        put("jpeg", "image/jpeg");
        put("png", "image/png");
        put("mp3", "audio/mpeg");
        put("m3u", "audio/mpeg-url");
        put("mp4", "video/mp4");
        put("ogv", "video/ogg");
        put("flv", "video/x-flv");
        put("mov", "video/quicktime");
        put("swf", "application/x-shockwave-flash");
        put("js", "application/javascript");
        put("pdf", "application/pdf");
        put("doc", "application/msword");
        put("ogg", "application/x-ogg");
        put("zip", "application/octet-stream");
        put("exe", "application/octet-stream");
        put("class", "application/octet-stream");
    }};
    
	private final File rootFolder;
	
	public FileHandler(File rootFolder) {
		super();
		this.rootFolder = rootFolder;
	}



	@Override
	public Response serve(IHTTPSession session) {
		String uri = session.getUri();
		
        // Remove URL arguments
        uri = uri.trim().replace( File.separatorChar, '/' );

        if ( uri.indexOf( '?' ) >= 0 )
            uri = uri.substring(0, uri.indexOf( '?' ));

        // Prohibit getting out of current directory
        if ( uri.startsWith( ".." ) || uri.endsWith( ".." ) || uri.indexOf( "../" ) >= 0 ) {
        	return createResponse(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: Won't serve ../ for security reasons.");
        }
		
		return respond(Collections.unmodifiableMap(session.getHeaders()), uri);
	}

    /**
     * URL-encodes everything between "/"-characters. Encodes spaces as '%20' instead of '+'.
     */
    private String encodeUri(String uri) {
        String newUri = "";
        StringTokenizer st = new StringTokenizer(uri, "/ ", true);
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if (tok.equals("/"))
                newUri += "/";
            else if (tok.equals(" "))
                newUri += "%20";
            else {
                try {
                    newUri += URLEncoder.encode(tok, "UTF-8");
                } catch (UnsupportedEncodingException ignored) {
                }
            }
        }
        return newUri;
    }

	
    private String findIndexFileInDirectory(File directory) {
        for (String fileName : INDEX_FILE_NAMES) {
            File indexFile = new File(directory, fileName);
            if (indexFile.exists()) {
                return fileName;
            }
        }
        return null;
    }
	
    private String listDirectory(String uri, File f) {
        String heading = "Directory " + uri;
        StringBuilder msg = new StringBuilder("<html><head><title>" + heading + "</title><style><!--\n" +
            "span.dirname { font-weight: bold; }\n" +
            "span.filesize { font-size: 75%; }\n" +
            "// -->\n" +
            "</style>" +
            "</head><body><h1>" + heading + "</h1>");

        String up = null;
        if (uri.length() > 1) {
            String u = uri.substring(0, uri.length() - 1);
            int slash = u.lastIndexOf('/');
            if (slash >= 0 && slash < u.length()) {
                up = uri.substring(0, slash + 1);
            }
        }

        List<String> files = Arrays.asList(f.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return new File(dir, name).isFile();
            }
        }));
        Collections.sort(files);
        List<String> directories = Arrays.asList(f.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return new File(dir, name).isDirectory();
            }
        }));
        Collections.sort(directories);
        if (up != null || directories.size() + files.size() > 0) {
            msg.append("<ul>");
            if (up != null || directories.size() > 0) {
                msg.append("<section class=\"directories\">");
                if (up != null) {
                    msg.append("<li><a rel=\"directory\" href=\"").append(up).append("\"><span class=\"dirname\">..</span></a></b></li>");
                }
                for (String directory : directories) {
                    String dir = directory + "/";
                    msg.append("<li><a rel=\"directory\" href=\"").append(encodeUri(uri + dir)).append("\"><span class=\"dirname\">").append(dir).append("</span></a></b></li>");
                }
                msg.append("</section>");
            }
            if (files.size() > 0) {
                msg.append("<section class=\"files\">");
                for (String file : files) {
                    msg.append("<li><a href=\"").append(encodeUri(uri + file)).append("\"><span class=\"filename\">").append(file).append("</span></a>");
                    File curFile = new File(f, file);
                    long len = curFile.length();
                    msg.append("&nbsp;<span class=\"filesize\">(");
                    if (len < 1024) {
                        msg.append(len).append(" bytes");
                    } else if (len < 1024 * 1024) {
                        msg.append(len / 1024).append(".").append(len % 1024 / 10 % 100).append(" KB");
                    } else {
                        msg.append(len / (1024 * 1024)).append(".").append(len % (1024 * 1024) / 10 % 100).append(" MB");
                    }
                    msg.append(")</span></li>");
                }
                msg.append("</section>");
            }
            msg.append("</ul>");
        }
        msg.append("</body></html>");
        return msg.toString();
    }

    // Get MIME type from file name extension, if possible
    protected String getMimeTypeForFile(String uri) {
        int dot = uri.lastIndexOf('.');
        String mime = null;
        if (dot >= 0) {
            mime = MIME_TYPES.get(uri.substring(dot + 1).toLowerCase());
        }
        return mime == null ? MIME_DEFAULT_BINARY : mime;
    }
	
    protected Response respond(Map<String, String> headers, String uri) {

        File f = new File(rootFolder, uri);
        boolean canServeUri = f.exists() && f.canRead();
        
        if (!canServeUri) {
            return createResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Error 404, file not found.");
        }

        // Browsers get confused without '/' after the directory, send a redirect.
        
        if (f.isDirectory() && !uri.endsWith("/")) {
            uri += "/";
            Response res = createResponse(Response.Status.REDIRECT, NanoHTTPD.MIME_HTML, "<html><body>Redirected: <a href=\"" +
                uri + "\">" + uri + "</a></body></html>");
            res.addHeader("Location", uri);
            return res;
        }

        if (f.isDirectory()) {
            // First look for index files (index.html, index.htm, etc) and if none found, list the directory if readable.
            String indexFile = findIndexFileInDirectory(f);
            if (indexFile == null) {
                if (f.canRead()) {
                    // No index file, list the directory if it is readable
                    return createResponse(Response.Status.OK, NanoHTTPD.MIME_HTML, listDirectory(uri, f));
                } else {
                    return createResponse(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: No directory listing.");
                }
            } else {
                return respond(headers, uri + indexFile);
            }
        }

        String mimeTypeForFile = getMimeTypeForFile(uri);
        
        String etag = Integer.toHexString((f.getAbsolutePath() + f.lastModified() + "" + f.length()).hashCode());
        Response response = null;
        try {        
        	response = serveStream(uri, headers, new FileInputStream(f), f.length(), mimeTypeForFile, etag);
        } catch (IOException ioe) {
        	response = createResponse(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: Reading file failed.");
        }
        
        return response != null ? response :
            createResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Error 404, file not found.");
    }
	
	
	
    protected Response serveStream(String uri, Map<String, String> header, InputStream stream, long length, String mime, String etag) throws IOException {
        Response res;

            // Support (simple) skipping:
            long startFrom = 0;
            long endAt = -1;
            String range = header.get("range");
            if (range != null) {
                if (range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length());
                    int minus = range.indexOf('-');
                    try {
                        if (minus > 0) {
                            startFrom = Long.parseLong(range.substring(0, minus));
                            endAt = Long.parseLong(range.substring(minus + 1));
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            // Change return code and add Content-Range header when skipping is requested
            long fileLen = length;
            if (range != null && startFrom >= 0) {
                if (startFrom >= fileLen) {
                    res = createResponse(Response.Status.RANGE_NOT_SATISFIABLE, NanoHTTPD.MIME_PLAINTEXT, "");
                    res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
                    res.addHeader("ETag", etag);
                } else {
                    if (endAt < 0) {
                        endAt = fileLen - 1;
                    }
                    long newLen = endAt - startFrom + 1;
                    if (newLen < 0) {
                        newLen = 0;
                    }

                    final long dataLen = newLen;
                    InputStream fis = new InputStreamWrapper(stream, dataLen);
                    fis.skip(startFrom);

                    res = createResponse(Response.Status.PARTIAL_CONTENT, mime, fis);
                    res.addHeader("Content-Length", "" + dataLen);
                    res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
                    res.addHeader("ETag", etag);
                }
            } else {
                if (etag.equals(header.get("if-none-match")))
                    res = createResponse(Response.Status.NOT_MODIFIED, mime, "");
                else {
                    res = createResponse(Response.Status.OK, mime, stream);
                    res.addHeader("Content-Length", "" + fileLen);
                    res.addHeader("ETag", etag);
                }
            }

        return res;
    }

    // Announce that the file server accepts partial content requests
    protected Response createResponse(Response.Status status, String mimeType, InputStream message) {
        Response res = new Response(status, mimeType, message);
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }

    // Announce that the file server accepts partial content requests
    protected Response createResponse(Response.Status status, String mimeType, String message) {
        Response res = new Response(status, mimeType, message);
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }

	private static class InputStreamWrapper extends InputStream {
		private final long available;
		private final InputStream inputStream;
		
		public InputStreamWrapper(InputStream inputStream, long available) {
			super();
			this.available = available;
			this.inputStream = inputStream;
		}

		@Override
		public int available() throws IOException {
			return (int) available;
		}

		@Override
		public void close() throws IOException {
			inputStream.close();
		}

		@Override
		public void mark(int readlimit) {
			inputStream.mark(readlimit);
		}

		@Override
		public boolean markSupported() {
			return inputStream.markSupported();
		}

		@Override
		public int read() throws IOException {
			return inputStream.read();
		}

		@Override
		public int read(byte[] buffer, int byteOffset, int byteCount)throws IOException {		
			return inputStream.read(buffer, byteOffset, byteCount);
		}

		@Override
		public int read(byte[] buffer) throws IOException {
			return inputStream.read(buffer);
		}

		@Override
		public synchronized void reset() throws IOException {
			inputStream.reset();
		}

		@Override
		public long skip(long byteCount) throws IOException {
			return inputStream.skip(byteCount);
		}
	}

	@Override
	public void start() {
		
	}



	@Override
	public void stop() {
		
	}

}
