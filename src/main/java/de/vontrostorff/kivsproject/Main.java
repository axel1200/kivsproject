package de.vontrostorff.kivsproject;

import de.vontrostorff.kivsproject.data_analysis.MainStatisticalWriter;
import de.vontrostorff.kivsproject.download.FileDownloader;
import de.vontrostorff.kivsproject.parsing.FileParser;
import de.vontrostorff.kivsproject.parsing.dtos.Ping;
import de.vontrostorff.kivsproject.parsing.dtos.PingFile;
import de.vontrostorff.kivsproject.plotting.IPv46PlotterAndWriter;
import de.vontrostorff.kivsproject.plotting.MainPlotter;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException {
        URL url = new URL(args[0]);
        BufferedReader readerForFile = FileDownloader.getReaderForFile(url);
        FileParser fileParser = new FileParser(readerForFile);
        PingFile pingFile = fileParser.parse();
        LOGGER.info("A file with " + pingFile.getPingGroups().size() + " ping groups were parsed");

        List<Float> flatPings = pingFile.getPingGroups().stream()
                .flatMap(pingGroup -> pingGroup.getPings().stream())
                .map(Ping::getRoundTripTime)
                .collect(Collectors.toList());

        Path outputDir = Paths.get(args[1]);
        outputDir.toFile().mkdirs();
        MainPlotter mainPlotter = new MainPlotter(pingFile, outputDir);
        mainPlotter.plotAll();

        MainStatisticalWriter mainStatisticalWriter = new MainStatisticalWriter(outputDir.resolve("general-stats.txt"), flatPings, pingFile.getPingGroups());
        mainStatisticalWriter.writeFile();

        Path ipv4_ipv6 = outputDir.resolve("ipv4_ipv6");
        ipv4_ipv6.toFile().mkdirs();
        IPv46PlotterAndWriter iPv46PlotterAndWriter = new IPv46PlotterAndWriter(pingFile, ipv4_ipv6);
        iPv46PlotterAndWriter.plotAndWrite();

    }
}