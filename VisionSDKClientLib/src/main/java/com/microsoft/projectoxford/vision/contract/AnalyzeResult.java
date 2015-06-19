package com.microsoft.projectoxford.vision.contract;

import java.util.List;
import java.util.UUID;

public class AnalyzeResult {
    public UUID requestId;

    public Metadata metadata;

    public ImageType imageType;

    public Color color;

    public Adult adult;

    public List<Category> categories;

    public List<Face> faces;
}
