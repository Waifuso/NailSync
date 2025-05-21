package com.example.androidexample;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom Volley request for handling multipart/form-data uploads with files and parameters
 */
public class MultipartRequest extends Request<String> {
    private static final String TAG = "MultipartRequest";
    private static final String BOUNDARY = "----" + System.currentTimeMillis();
    private static final String LINE_FEED = "\r\n";
    private final Response.Listener<String> mListener;
    private final byte[] mImageData;
    private Map<String, String> mParams;
    private Map<String, String> mHeaders;

    /**
     * Constructor
     *
     * @param method        Request method (usually POST)
     * @param url           URL to send the request to
     * @param imageData     Image data as byte array (can be null for text-only posts)
     * @param listener      Response listener
     * @param errorListener Error listener
     */
    public MultipartRequest(int method, String url, byte[] imageData,
                            Response.Listener<String> listener,
                            Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
        mImageData = imageData;
        mParams = new HashMap<>();
        mHeaders = new HashMap<>();
    }

    /**
     * Set additional parameters to be sent with the request
     *
     * @param params Map of parameters
     */
    public void setParams(Map<String, String> params) {
        mParams = params;
    }

    /**
     * Set additional headers to be sent with the request
     *
     * @param headers Map of headers
     */
    public void setHeaders(Map<String, String> headers) {
        mHeaders = headers;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mHeaders != null ? mHeaders : super.getHeaders();
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data; boundary=" + BOUNDARY;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        try {
            // Add string parameters
            for (Map.Entry<String, String> entry : mParams.entrySet()) {
                buildTextPart(dos, entry.getKey(), entry.getValue());
            }

            // Add image data if available
            if (mImageData != null) {
                buildFilePart(dos, "file", "image.jpg");
                dos.write(mImageData);
                dos.writeBytes(LINE_FEED);
            }

            // End of multipart/form-data
            dos.writeBytes("--" + BOUNDARY + "--" + LINE_FEED);

            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Builds a text part of the multipart request
     */
    private void buildTextPart(DataOutputStream dataOutputStream, String paramName, String paramValue) throws IOException {
        dataOutputStream.writeBytes("--" + BOUNDARY + LINE_FEED);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + paramName + "\"" + LINE_FEED);
        dataOutputStream.writeBytes("Content-Type: text/plain; charset=UTF-8" + LINE_FEED);
        dataOutputStream.writeBytes(LINE_FEED);
        dataOutputStream.writeBytes(paramValue + LINE_FEED);
    }

    /**
     * Builds a file part of the multipart request
     */
    private void buildFilePart(DataOutputStream dataOutputStream, String paramName, String fileName) throws IOException {
        dataOutputStream.writeBytes("--" + BOUNDARY + LINE_FEED);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + paramName + "\"; filename=\"" + fileName + "\"" + LINE_FEED);
        dataOutputStream.writeBytes("Content-Type: image/jpeg" + LINE_FEED);
        dataOutputStream.writeBytes("Content-Transfer-Encoding: binary" + LINE_FEED);
        dataOutputStream.writeBytes(LINE_FEED);
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            String responseString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }
}