package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class GirlsOfDesireRipper extends AbstractHTMLRipper {
    // Current HTML document
    private Document albumDoc = null;

    public GirlsOfDesireRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "bitchesgirls";
    }
    @Override
    public String getDomain() {
        return "bitchesgirls.com";
    }

    public String getAlbumTitle(URL url) throws MalformedURLException {
        try {
            // Attempt to use album title as GID
            Document doc = getFirstPage();
            Elements elems = doc.select(".albumName");
            return getHost() + "_" + elems.first().text();
        } catch (Exception e) {
            // Fall back to default album naming convention
            LOGGER.warn("Failed to get album title from " + url, e);
        }
        return super.getAlbumTitle(url);
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p;
        Matcher m;

        p = Pattern.compile("^bitchesgirls\\.com/[a-zA-Z]+/[a-zA-Z]+/([a-zA-Z]+(-[a-zA-Z]+)+)-(0?[1-9]|[12][0-9]|3[01])([+-]?(?=\.\d|\d)(?:\d+)?(?:\.?\d*))(?:[eE]([+-]?\d+))?/(0?[1-9]|[1][0-2])/$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected bitchesgirls format: "
                        + "https://bitchesgirls.com/<media>/<name>/<name>/"
                        + " Got: " + url);
    }

    @Override
    public Document getFirstPage() throws IOException {
        if (albumDoc == null) {
            albumDoc = Http.url(url).get();
        }
        return albumDoc;
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> imageURLs = new ArrayList<>();
        for (Element thumb : doc.select("albumgrid > a > img")) {
            String imgSrc = thumb.attr("src");
            imgSrc = imgSrc.replaceAll("_thumbnail\\.", ".");
            if (imgSrc.startsWith("/")) {
                imgSrc = "https://cdn2.bitchesgirls.com/" + imgSrc;
            }
            imageURLs.add(imgSrc);
        }
        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        // Send referrer when downloading images
        addURLToDownload(url, getPrefix(index), "", this.url.toExternalForm(), null);
    }
}
