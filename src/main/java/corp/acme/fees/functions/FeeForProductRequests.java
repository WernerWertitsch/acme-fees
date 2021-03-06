package corp.acme.fees.functions;

import corp.acme.common.domain.ProductRequest;
import corp.acme.fees.FeeService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FeeForProductRequests implements Function<List<ProductRequest>, List<Double>> {
    @Autowired
    FeeService feeService;

    @Override
    public List<Double> apply(List<ProductRequest> productRequests) {
        return productRequests.stream().map(r ->
                feeService.getFeeForCategoryAndValue(r.getClassification().getCategory().getId(), r.getValue())).collect(Collectors.toList());
    }

}
