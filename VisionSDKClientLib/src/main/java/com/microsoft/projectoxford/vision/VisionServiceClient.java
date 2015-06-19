package com.microsoft.projectoxford.vision;

import com.microsoft.projectoxford.vision.contract.AnalyzeResult;
import com.microsoft.projectoxford.vision.contract.OCR;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;

import java.io.IOException;
import java.io.InputStream;

public interface VisionServiceClient {
    public AnalyzeResult analyzeImage(String url, String[] visualFeatures) throws VisionServiceException;

    public AnalyzeResult analyzeImage(InputStream stream, String[] visualFeatures) throws VisionServiceException, IOException;

    public OCR recognizeText(String url, String languageCode, boolean detectOrientation) throws VisionServiceException;

    public OCR recognizeText(InputStream stream, String languageCode, boolean detectOrientation) throws VisionServiceException, IOException;

    public byte[] getThumbnail(int width, int height, boolean smartCropping, String url) throws VisionServiceException, IOException;

    public byte[] getThumbnail(int width, int height, boolean smartCropping, InputStream stream) throws VisionServiceException, IOException;
}
