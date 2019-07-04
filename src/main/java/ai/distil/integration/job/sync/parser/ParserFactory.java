package ai.distil.integration.job.sync.parser;

import ai.distil.integration.job.sync.AbstractConnection;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;

@Slf4j
@Service
public class ParserFactory {

    public static AbstractParser buildParser(@NotNull AbstractConnection connection, @NotNull DataSourceDataHolder dataSource, ParserType parserType) {

        switch (parserType) {
            case SIMPLE:
                return new SimpleParser(connection, dataSource);
            default:
                String msg = String.format("Looks like there is no parser, for parser type %s", parserType);
                throw new UnsupportedOperationException(msg);
        }
    }

}
