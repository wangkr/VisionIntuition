package com.microsoft.projectoxford.vision;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.contract.AnalyzeResult;
import com.microsoft.projectoxford.vision.contract.OCR;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;
import com.microsoft.projectoxford.vision.rest.WebServiceRequest;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class VisionServiceRestClient implements VisionServiceClient {
    private static final String serviceHost = "https://api.projectoxford.ai/vision/v1";
    private WebServiceRequest restCall = null;
    private Gson gson = new Gson();

    public VisionServiceRestClient(String subscriptKey) {
        this.restCall = new WebServiceRequest(subscriptKey);
    }

    @Override
    public AnalyzeResult analyzeImage(String url, String[] visualFeatures) throws VisionServiceException {
        Map<String, Object> params = new HashMap<>();
        String features = TextUtils.join(",", visualFeatures);
        params.put("visualFeatures", features);
        String path = serviceHost + "/analyses";
        String uri = WebServiceRequest.getUrl(path, params);

        params.clear();
        params.put("url", url);

        String json = (String) this.restCall.request(uri, "POST", params, null, false);
        AnalyzeResult visualFeature = this.gson.fromJson(json, AnalyzeResult.class);

        return visualFeature;
    }

    @Override
    public AnalyzeResult analyzeImage(InputStream stream, String[] visualFeatures) throws VisionServiceException, IOException {
        Map<String, Object> params = new HashMap<>();
        String features = TextUtils.join(",", visualFeatures);
        params.put("visualFeatures", features);
        String path = serviceHost + "/analyses";
        String uri = WebServiceRequest.getUrl(path, params);

        params.clear();
        byte[] data = IOUtils.toByteArray(stream);
        params.put("data", data);

        String json = (String) this.restCall.request(uri, "POST", params, "application/octet-stream", false);
        AnalyzeResult visualFeature = this.gson.fromJson(json, AnalyzeResult.class);

        return visualFeature;
    }

    @Override
    public OCR recognizeText(String url, String languageCode, boolean detectOrientation) throws VisionServiceException {
        Map<String, Object> params = new HashMap<>();
        params.put("language", languageCode);
        params.put("detectOrientation", detectOrientation);
        String path = serviceHost + "/ocr";
        String uri = WebServiceRequest.getUrl(path, params);

        params.clear();
        params.put("url", url);
        String json = (String) this.restCall.request(uri, "POST", params, null, false);
        OCR ocr = this.gson.fromJson(json, OCR.class);

        return ocr;
    }

    @Override
    public OCR recognizeText(InputStream stream, String languageCode, boolean detectOrientation) throws VisionServiceException, IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("language", languageCode);
        params.put("detectOrientation", detectOrientation);
        String path = serviceHost + "/ocr";
        String uri = WebServiceRequest.getUrl(path, params);

        byte[] data = IOUtils.toByteArray(stream);
        params.put("data", data);
        String json = (String) this.restCall.request(uri, "POST", params, "application/octet-stream", false);
        OCR ocr = this.gson.fromJson(json, OCR.class);

        return ocr;
    }

    @Override
    public byte[] getThumbnail(int width, int height, boolean smartCropping, String url) throws VisionServiceException, IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("width", width);
        params.put("height", height);
        params.put("smartCropping", smartCropping);
        String path = serviceHost + "/thumbnails";
        String uri = WebServiceRequest.getUrl(path, params);

        params.clear();
        params.put("url", url);

        InputStream is = (InputStream)this.restCall.request(uri, "POST", params, null, true);
        byte[] image = IOUtils.toByteArray(is);
        if (is != null) {
            is.close();
        }

        return image;
    }

    @Override
    public byte[] getThumbnail(int width, int height, boolean smartCropping, InputStream stream) throws VisionServiceException, IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("width", width);
        params.put("height", height);
        params.put("smartCropping", smartCropping);
        String path = serviceHost + "/thumbnails";
        String uri = WebServiceRequest.getUrl(path, params);

        params.clear();
        byte[] data = IOUtils.toByteArray(stream);
        params.put("data", data);

        InputStream is = (InputStream)this.restCall.request(uri, "POST", params, "application/octet-stream", true);
        byte[] image = IOUtils.toByteArray(is);
        if (is != null) {
            is.close();
        }

        return image;
    }
}
