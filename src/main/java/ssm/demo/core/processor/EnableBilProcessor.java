package ssm.demo.core.processor;

import ssm.demo.core.processor.configuration.ProcessorConfiguration;
import ssm.demo.core.processor.service.ProcessJob;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Import({ProcessorConfiguration.class, ProcessJob.class})
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface EnableBilProcessor {
}
