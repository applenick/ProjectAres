package tc.oc.pgm.rush;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import org.jdom2.Document;
import org.jdom2.Element;

import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.pgm.ffa.FreeForAllModule;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModuleFactory;
import tc.oc.pgm.module.ModuleDescription;
import tc.oc.pgm.module.ModuleLoadException;
import tc.oc.pgm.regions.Region;
import tc.oc.pgm.regions.RegionParser;
import tc.oc.pgm.score.ScoreModule;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;

@ModuleDescription(name = "Rush", follows = { FreeForAllModule.class })
public class RushModule implements MapModule, MatchModuleFactory<RushMatchModule> {

    private final RushConfig config;

    public RushModule(@Nonnull RushConfig config) {
        this.config = config;
    }

    @Override
    public Set<MapDoc.Gamemode> getGamemodes(MapModuleContext context) {
        return Collections.singleton(MapDoc.Gamemode.rush);
    }

    @Override
    public RushMatchModule createMatchModule(Match match) throws ModuleLoadException {
        return new RushMatchModule(match, config);
    }

    public static RushModule parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
        Element rushElement = doc.getRootElement().getChild("rush");

        if (rushElement == null) {
            return null;
        }

        int timeLimit = XMLUtils.parseNumber(rushElement, "time-limit", Integer.class).required();
        boolean regenerate = XMLUtils.parseBoolean(rushElement, "regenerate").optional(false);
        
        final RegionParser regionParser = context.needModule(RegionParser.class);
        Region startLine = regionParser.property(rushElement, "start-line").alias("start").union();
        Region finishLine = regionParser.property(rushElement, "finish-line").alias("finish").union();

        return new RushModule(new RushConfig(timeLimit, regenerate, startLine, finishLine));
    }
}
