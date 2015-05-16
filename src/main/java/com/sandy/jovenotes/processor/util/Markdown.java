package com.sandy.jovenotes.processor.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.markdown4j.CodeBlockEmitter;
import org.markdown4j.ExtDecorator;
import org.markdown4j.IncludePlugin;
import org.markdown4j.Markdown4jProcessor;
import org.markdown4j.Plugin;
import org.markdown4j.WebSequencePlugin;
import org.markdown4j.YumlPlugin;

import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Configuration.Builder;
import com.github.rjeschke.txtmark.Processor;

/**
 * This is a clone of the {@link Markdown4jProcessor} class with the option of
 * converting a newline to br tag removed. 
 * 
 * @author Sandeep
 */
public class Markdown {
	
	private Builder builder;
	
	private ExtDecorator decorator;
	
	public Markdown() {
		this.builder = builder();
	}
	
	private Builder builder() {
		decorator = new ExtDecorator();
		return Configuration.builder()
							.forceExtentedProfile()
							.registerPlugins(new YumlPlugin(), new WebSequencePlugin(), new IncludePlugin())
							.setDecorator(decorator)
							.setCodeBlockEmitter(new CodeBlockEmitter());
	}
	
	public Markdown registerPlugins(Plugin ... plugins) {
		builder.registerPlugins(plugins);
		return this;
	}
	
	public Markdown setDecorator(ExtDecorator decorator) {
		this.decorator = decorator;
		builder.setDecorator(decorator);
		return this;
	}
	
	public Markdown addHtmlAttribute(String name, String value, String ...tags) {
		decorator.addHtmlAttribute(name, value, tags);
		return this;
	}
	
	public Markdown addStyleClass(String styleClass, String ...tags) {
		decorator.addStyleClass(styleClass, tags);
		return this;
	}
	
	public String process(File file) throws IOException {
		return Processor.process(file, builder.build());
	}
	
	public String process(InputStream input) throws IOException {
		return Processor.process(input);
	}
	
	public String process(Reader reader) throws IOException {
		return Processor.process(reader, builder.build());
	}
	
	public String process(String input) throws IOException {
		return Processor.process(input, builder.build());
	}
}

