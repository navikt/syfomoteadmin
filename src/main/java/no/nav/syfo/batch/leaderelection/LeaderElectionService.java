package no.nav.syfo.batch.leaderelection;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.syfo.metric.Metrikk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.io.IOException;
import java.net.InetAddress;


@Service
public class LeaderElectionService {
    private static final Logger log = LoggerFactory.getLogger(LeaderElectionService.class);

    private final Metrikk metrikk;
    private final RestTemplate restTemplate;
    private final String electorpath;

    @Inject
    public LeaderElectionService(
            Metrikk metrikk,
            RestTemplate restTemplate,
            @Value("${elector.path}") String electorpath
    ) {
        this.metrikk = metrikk;
        this.restTemplate = restTemplate;
        this.electorpath = electorpath;
    }

    public boolean isLeader() {
        metrikk.countEvent("isLeader_kalt");
        ObjectMapper objectMapper = new ObjectMapper();
        String url = "http://" + electorpath;

        String response = restTemplate.getForObject(url, String.class);

        try {
            LeaderPod leader = objectMapper.readValue(response, LeaderPod.class);
            return isHostLeader(leader);
        } catch (IOException e) {
            log.error("Couldn't map response from electorPath to LeaderPod object", e);
            metrikk.countEvent("isLeader_feilet");
            throw new RuntimeException("Couldn't map response from electorpath to LeaderPod object", e);
        } catch (Exception e) {
            log.error("Something went wrong when trying to check leader", e);
            metrikk.countEvent("isLeader_feilet");
            throw new RuntimeException("Got exception when trying to find leader", e);
        }
    }

    private boolean isHostLeader(LeaderPod leader) throws Exception {
        String hostName = InetAddress.getLocalHost().getHostName();
        String leaderName = leader.getName();

        if (hostName.equals(leaderName)) {
            log.info("Host with name {} is leader", hostName);
            return true;
        }
        log.info("Host with name {} is not leader {}", hostName, leaderName);
        return false;
    }
}
