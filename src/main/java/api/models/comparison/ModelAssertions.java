package api.models.comparison;

import org.assertj.core.api.AbstractAssert;

public class ModelAssertions extends AbstractAssert<ModelAssertions, Object> {
    private final Object request;
    private final Object response;
    private double epsilon = 0.01;

    private ModelAssertions(Object request, Object response) {
        super(request, ModelAssertions.class);
        this.request = request;
        this.response = response;
    }

    public static ModelAssertions assertThatModel(Object request, Object response) {
        return new ModelAssertions(request, response);
    }

    public ModelAssertions withEpsilon(double epsilon) {
        this.epsilon = epsilon;
        return this;
    }

    public ModelAssertions match() {
        ModelComparisonConfigLoader configLoader = new ModelComparisonConfigLoader("model-comparison.properties");
        ModelComparisonConfigLoader.ComparisonRule rule = configLoader.getRuleFor(request.getClass());

        if (rule != null) {
            ModelComparator.ComparisonResult result = ModelComparator.compareFields(
                    request,
                    response,
                    rule.getFieldMappings(),
                    epsilon
            );

            if (!result.isSuccess()) {
                failWithMessage("DTO comparison failed with mismatches:\n" + result);
            }
        } else {
            failWithMessage("No comparison rule found");
        }

        return this;
    }
}