package org.eclipse.jetty.server;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.MainServlet;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import io.github.toolfactory.narcissus.Narcissus;

public class Main {

	private static class MethodVisitorImpl extends MethodVisitor {

		private Object value = null;

		private Collection<Object> values = null;

		private MethodVisitorImpl() {
			//
			super(Opcodes.ASM9);
			//
		}

		@Override
		public void visitLdcInsn(final Object value) {
			this.value = value;
		}

		@Override
		public void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor,
				final boolean isInterface) {
			//
			if (Objects.equals("getAttribute", name)
					&& Objects.equals(descriptor,
							"(Ljavax/servlet/ServletContext;Ljava/lang/String;)Ljava/lang/Object;")
					&& (values = ObjectUtils.getIfNull(values, ArrayList::new)) != null && !values.contains(value)) {
				//
				values.add(value);
				//
			} // if
				//
		}

	}

	public static void main(final String[] args) throws Exception {
		//
		final ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		//
		servletContextHandler.setContextPath("/");
		//
		final Class<?> clz = MainServlet.class;
		//
		final Map<String, String> map = toMap(args);
		//
		try (final InputStream is = getResourceAsStream(clz,
				StringUtils.join("/", replace(getName(clz), '.', '/'), ".class"))) {
			//
			final ClassReader cr = testAndApply(Objects::nonNull, is, ClassReader::new, null);
			//
			if (cr != null) {
				//
				final MethodVisitorImpl mvi = new MethodVisitorImpl();
				//
				cr.accept(new ClassVisitor(Opcodes.ASM9) {

					@Override
					public MethodVisitor visitMethod(final int access, final String name, final String descriptor,
							final String signature, final String[] exceptions) {
						//
						return mvi;
						//
					}

				}, 0);
				//
				final Iterable<Object> iterable = mvi.values;
				//
				String string = null;
				//
				for (int i = 0; i < IterableUtils.size(iterable); i++) {
					//
					servletContextHandler.setAttribute(string = Objects.toString(IterableUtils.get(iterable, i)),
							MapUtils.getObject(map, string));
					//
				} // for
					//
			} // if
				//
		} // try
			//
		final ServletHolder servletHolder = new ServletHolder(new MainServlet());
		//
		servletContextHandler.addServlet(servletHolder, "/");
		//
		final Server server = new Server(NumberUtils.toInt(MapUtils.getString(map, "port"), 8080));
		//
		server.setHandler(servletContextHandler);
		//
		if (!isTestMode()) {
			//
			server.start();
			//
			server.join();
			//
		} // if
			//
	}

	private static InputStream getResourceAsStream(final Class<?> clz, final String name) {
		//
		if (clz == null) {
			//
			return null;
			//
		} // if
			//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(name), FieldUtils::getAllFieldsList, null)),
						f -> Objects.equals(getName(f), "value")), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		return field == null || Narcissus.getField(name, field) != null ? clz.getResourceAsStream(name) : null;
		//
	}

	private static String replace(final String instance, final char oldChar, final char newChar) {
		//
		if (instance == null) {
			//
			return null;
			//
		} // if
			//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						f -> Objects.equals(getName(f), "value")), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		return field == null || Narcissus.getField(instance, field) != null ? instance.replace(oldChar, newChar) : null;
		//
	}

	private static String getName(final Class<?> instance) {
		return instance != null ? instance.getName() : null;
	}

	private static boolean isTestMode() {
		try {
			return Class.forName("org.testng.annotations.Test") != null;
		} catch (final ClassNotFoundException e) {
			return false;
		}
	}

	private static Map<String, String> toMap(final String... ss) {
		//
		Map<String, String> map = null;
		//
		Entry<String, String> entry = null;
		//
		for (int i = 0; i < length(ss); i++) {
			//
			if ((entry = toEntry(ArrayUtils.get(ss, i))) == null) {
				//
				continue;
				//
			} // if
				//
			put(map = ObjectUtils.getIfNull(map, LinkedHashMap::new), getKey(entry), getValue(entry));
			//
		} // for
			//
		return map;
		//
	}

	private static <K> K getKey(final Entry<K, ?> instance) {
		return instance != null ? instance.getKey() : null;
	}

	private static <V> V getValue(final Entry<?, V> instance) {
		return instance != null ? instance.getValue() : null;
	}

	private static Entry<String, String> toEntry(final String string) {
		//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(string), FieldUtils::getAllFieldsList, null)),
						f -> Objects.equals(getName(f), "value")), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		if (string != null && field != null && Narcissus.getField(string, field) == null) {
			//
			return null;
			//
		} // if
			//
		if (Objects.equals(string, "=")) {
			//
			return Pair.of("", "");
			//
		} else if (string != null && string.length() == 2 && string.charAt(0) == '=') {
			//
			return Pair.of("", string.substring(1, string.length()));
			//
		} else if (string != null && string.length() == 2 && string.charAt(string.length() - 1) == '=') {
			//
			return Pair.of(string.substring(0, string.length() - 1), "");
			//
		} else if (string != null && string.indexOf('=') >= 0 && string.indexOf('=') == string.lastIndexOf('=')) {
			//
			return Pair.of(StringUtils.substringBefore(string, '='), StringUtils.substringAfter(string, '='));
			//
		} else if (string != null && string.length() > 2 && string.indexOf('=') != string.lastIndexOf('=')) {
			//
			return Pair.of(StringUtils.substring(string, 0, string.indexOf('=')),
					StringUtils.substring(string, string.indexOf('=') + 1));
			//
		} // if
			//
		return null;
		//
	}

	private static String getName(final Member instance) {
		return instance != null ? instance.getName() : null;
	}

	private static <T, R, A> R collect(final Stream<T> instance, final Collector<? super T, A, R> collector) {
		return instance != null && (collector != null || Proxy.isProxyClass(getClass(instance)))
				? instance.collect(collector)
				: null;
	}

	private static <T> Stream<T> filter(final Stream<T> instance, final Predicate<? super T> predicate) {
		return instance != null ? instance.filter(predicate) : instance;
	}

	private static <T> Stream<T> stream(final Collection<T> instance) {
		return instance != null ? instance.stream() : null;
	}

	private static <T, R, E extends Throwable> R testAndApply(final Predicate<T> predicate, final T value,
			final FailableFunction<T, R, E> functionTrue, final FailableFunction<T, R, E> functionFalse) throws E {
		return test(predicate, value) ? apply(functionTrue, value) : apply(functionFalse, value);
	}

	private static <T> boolean test(final Predicate<T> instance, final T value) {
		return instance != null && instance.test(value);
	}

	private static <T, R, E extends Throwable> R apply(final FailableFunction<T, R, E> instance, final T value)
			throws E {
		return instance != null ? instance.apply(value) : null;
	}

	private static Class<?> getClass(final Object instance) {
		return instance != null ? instance.getClass() : null;
	}

	private static <K, V> void put(final Map<K, V> instance, final K key, final V value) {
		if (instance != null) {
			instance.put(key, value);
		}
	}

	private static int length(final Object[] instance) {
		return instance != null ? instance.length : 0;
	}

}