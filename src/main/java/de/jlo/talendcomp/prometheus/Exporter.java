package de.jlo.talendcomp.prometheus;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.exporter.common.TextFormat;

public class Exporter {

	private Map<String, Gauge> gauges = new HashMap<>();
	private Map<String, Histogram> histograms = new HashMap<>();
	private Server server;

	public Exporter() {
	}

	public void registerGauge(String metricName, String help, String labelName) {
		Gauge g = gauges.get(metricName);
		if (g == null) {
			Gauge.Builder b = Gauge.build(metricName, help);
			if (labelName != null) {
				b.labelNames(labelName);
			}
			g = b.register();
			gauges.put(metricName, g);
		}
	}

	public void setGaugeValue(String metricName, String help, String label, Number value) {
		Gauge gauge = gauges.get(metricName);
		if (gauge == null) {
			throw new IllegalStateException("Gauge " + metricName + " is not registered");
		}
		if (value != null) {
			if (label != null) {
				gauge
					.labels(label)
					.set(value.doubleValue());
			} else {
				gauge.set(value.doubleValue());
			}
		}
	}

	public void registerHistogram(String metricName, String help, String... labels) {
		Histogram g = histograms.get(metricName);
		if (g == null) {
			Histogram.Builder b = Histogram.build(metricName, help);
			if (labels != null) {
				b.labelNames(labels);
			}
			g = b.register();
			histograms.put(metricName, g);
		}
	}

	public void setHistogramValue(String metricName, String label, double value) {
		Histogram g = histograms.get(metricName);
		if (g == null) {
			throw new IllegalStateException("Histogram: " + metricName + " is not registered");
		}
		if (label != null) {
			g.labels(label).observe(value);
		} else {
			g.observe(value);
		}
	}
	
	public String getCurrentMetricSamples() throws IOException {
		StringWriter writer = new StringWriter();
		TextFormat.write004(writer, CollectorRegistry.defaultRegistry.filteredMetricFamilySamples(Collections.emptySet()));
		return writer.toString();
	}

	public void startServer(int port) throws Exception {
		if (port < 1) {
			throw new IllegalArgumentException("Post must be greater 0");
		}
		server = new Server(port);
		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		server.setHandler(context);
		// Expose Prometheus metrics.
		context.addServlet(new ServletHolder(new MetricsServlet()), "/metrics");
		// Add metrics about CPU, JVM memory etc.
		// we do not need the metrics of THIS jvm!
		//DefaultExports.initialize();
		// Start the webserver.
		server.start();
	}
	
	public void stopServer() {
		if (server != null) {
			try {
				server.stop();
			} catch (Exception e) {
				// ignore
			}
		}
	}

}
