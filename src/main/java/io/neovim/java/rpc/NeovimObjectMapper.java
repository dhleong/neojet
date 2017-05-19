package io.neovim.java.rpc;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.neovim.java.Rpc;
import io.neovim.java.rpc.impl.NeovimExtensionTypes;
import io.neovim.java.rpc.impl.NeovimMapperModule;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.util.Collections;
import java.util.Map;

/**
 * @author dhleong
 */
public class NeovimObjectMapper {
    private NeovimObjectMapper() {
        // no instances please
    }

    public static ObjectMapper newInstance() {
        return newInstance(null, Collections.emptyMap());
    }

    public static ObjectMapper newInstance(Rpc rpc, Map<Integer, Class<?>> requestedTypes) {
        NeovimMapperModule module = new NeovimMapperModule(rpc, requestedTypes);

        MessagePackFactory factory = new MessagePackFactory();
        factory.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        factory.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        factory.setExtTypeCustomDesers(
            NeovimExtensionTypes.getInstance()
                .getExtensionDeserializers(factory, module)
        );

        ObjectMapper mapper = new ObjectMapper(factory);
        mapper.registerModule(module);
        return mapper;
    }

}
