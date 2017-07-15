package tc.oc.pgm.proximity;

import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import tc.oc.time.Time;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.Repeatable;

public class ProximityAlarmMatchModule extends MatchModule {

    protected final Set<ProximityAlarm> proximityAlarms = Sets.newHashSet();

    public ProximityAlarmMatchModule(Match match, Set<ProximityAlarmDefinition> definitions) {
        super(match);

        proximityAlarms.addAll(definitions.stream()
                                          .map(definition -> new ProximityAlarm(match, definition, match.getRandom()))
                                          .collect(Collectors.toSet()));
    }

    @Override
    public void load() {
        proximityAlarms.forEach(match::registerEventsAndRepeatables);
    }

    @Repeatable(interval = @Time(ticks = 3))
    public void repeat() {
        if(!match.isRunning()) return;
        
        this.proximityAlarms.forEach(ProximityAlarm::showAlarm);
    }
}
