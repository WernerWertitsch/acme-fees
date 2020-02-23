package corp.acme.fees;

import corp.acme.common.domain.Category;
import corp.acme.common.domain.Classification;
import corp.acme.common.domain.Fee;
import corp.acme.common.domain.Util;
import corp.acme.common.util.ServiceCall;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/* This would usally be some kind of real time (trading?) data acess, or at least a non/relational database

 */
@Service
public class FeeService {

    public static final long WAIT_BEFORE_INITIALIZING = 300;


    Logger logger = Logger.getLogger("FeeService");

    @Value("classpath:static/fees.csv")
    Resource resourceFile;

    @Autowired
    private DiscoveryClient discoveryClient;

    /* THIS SHOULD BE IN SOME KIND OF DATABASE, THIS ONE MIGHT BE A RELATIONAL ONE */

    Map<String, Map<BigDecimal, Double>> mapped = new HashMap<>();
    List<Fee> all = new ArrayList<>();



    public Double getFeeForCategoryAndValue(String categoryId, BigDecimal value) {
        Map<BigDecimal, Double> map = this.mapped.get(categoryId);
        BigDecimal key = Arrays.asList(map.keySet().toArray(new BigDecimal[0]))
                .stream().filter(v -> v.compareTo(value) <= 0).findFirst().get();
        return map.get(key != null ? key : new BigDecimal(0));
    }



    @PostConstruct
    private void init() throws IOException {
        // I know.. :(, but need to wait for discovery and catgoeries, these would be separate micro services
        try {
            Thread.sleep(WAIT_BEFORE_INITIALIZING );
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        this.importFromFile();
    }


    private Category fetchCategory(String name){
        this.discoveryClient.getServices().forEach(s -> logger.info(String.format("Found Service %s", s)));
        URI uri = this.discoveryClient.getInstances("REGULATORY").get(0).getUri();
        // Hm, do I really have to do this manually?
        WebClient.RequestHeadersSpec call = ServiceCall.buildDefaultCall(uri, "byName", name);
        //omg, blocking, but this is just at startup
        logger.info(call.toString());
        return call.retrieve().toEntity(Category.class).block().getBody();
    }


    /* Stateful oldskool import */
    private void importFromFile() throws IOException {
        Category generic = new Category();
        generic.setDescription("No match was found for product, this is the generic rate");
        generic.setId("0");
        generic.setName("Generic");
        try {
            final Integer counter = new Integer(0);
            Arrays.asList(Util.asString(resourceFile).split("\r\n")).stream().map(l ->
                    l.split(";")
            ).collect(Collectors.toList()).forEach(line -> {
                Fee fee = new Fee();
                fee.setCategory(line[0].toLowerCase().equals("generic") ? generic : fetchCategory(line[0]));
                fee.setCeiling(line[2].equals("unlimited") ?
                        new BigDecimal(0) :
                        BigDecimal.valueOf(Long.parseLong(line[2].trim()
                                .replaceAll(" ", "")
                                .replaceAll(",", ".")
                )));
                fee.setFeePrct(Double.valueOf(Double.parseDouble(line[3]
                                .replaceAll("%", "")
                                .replaceAll(",", ".")
                )));
                addMapped(fee);
                all.add(fee);
                logger.info("Added "+fee);
            });

        } catch (Exception e) {
            logger.severe("Could not load or parse the fee csv!");
            logger.throwing("FeeService", "importFromFile", e);
            e.printStackTrace();
            throw e;
        }
    }

    private void addMapped(Fee fee) {
        Map<BigDecimal, Double> map = this.mapped.get(fee.getCategory().getId());
        if(map == null) {
            map = new HashMap<>();
            this.mapped.put(fee.getCategory().getId(), map);
        }
        map.put(fee.getCeiling(), fee.getFeePrct());
    }

}

