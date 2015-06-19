package com.microsoft.projectoxford.vision.contract;

import java.util.List;

public class Line {
    public boolean isVertical;

    public List<Word> words;

    public String boundingBox; //e.g. "boundingBox":"27, 66, 72, 18"
}
