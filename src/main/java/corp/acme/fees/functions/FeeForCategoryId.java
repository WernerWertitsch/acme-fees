package corp.acme.fees.functions;

import corp.acme.common.domain.Category;
import corp.acme.common.domain.Fee;
import corp.acme.common.domain.ProductRequest;
import corp.acme.fees.FeeService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class FeeForCategoryId implements Function<ProductRequest, Double> {
    @Autowired
    FeeService feeService;

    @Override
    public Double apply(ProductRequest productRequest) {
        return feeService.getFeeForCategoryAndValue(productRequest.getCategoryId(), productRequest.getValue());
    }
}
