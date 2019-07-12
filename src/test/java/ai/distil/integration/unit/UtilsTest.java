package ai.distil.integration.unit;

import ai.distil.integration.utils.ListUtils;
import ai.distil.integration.utils.MapUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class UtilsTest {

    @Test
    public void mapFlattenTest() {
        Map<String, Object> expectedResult = new HashMap<String, Object>() {{
            this.put("listfortransform_3_int", 3);
            this.put("string", "value");
            this.put("map_string", "value");
            this.put("map_int", 1);
            this.put("listfortransform_1_int", 1);
            this.put("long", 1L);
            this.put("listfortransform_2_string", "value");
            this.put("twolevelsmap_string", "value");
            this.put("twolevelsmap_innermap_string", "value");
            this.put("listfortransform_1_string", "value");
            this.put("listfortransform_3_string", "value");
            this.put("twolevelsmap_innermap_int", 1);
            this.put("listfortransform_2_int", 2);
            this.put("twolevelsmap_int", 1);
        }};

        Map<String, Object> map = buildComplexMap();
        Map<String, Object> flattedMap = MapUtils.flatten(map, ImmutableMap.of("listfortransform", mapsList ->
                (Map) ListUtils.groupByWithOverwrite(mapsList, m -> String.valueOf(m.get("int")), false)));

        Assertions.assertEquals(expectedResult, flattedMap);
    }

    private Map<String, Object> buildComplexMap() {
        Map<String, Object> twoLevelMap = buildSimpleMap(1);

        twoLevelMap.put("innermap", buildSimpleMap(1));

        return new HashMap<String, Object>() {{
            this.put("long", 1L);
            this.put("string", "value");
            this.put("map", buildSimpleMap(1));
            this.put("twolevelsmap", twoLevelMap);
            this.put("list", Lists.newArrayList("1", "2"));
            this.put("listfortransform", Lists.newArrayList(buildSimpleMap(1), buildSimpleMap(2), buildSimpleMap(3)));
        }};

    }

    private Map<String, Object> buildSimpleMap(int i) {
        return new HashMap<String, Object>() {{
            this.put("int", i);
            this.put("string", "value");
        }};
    }
}
