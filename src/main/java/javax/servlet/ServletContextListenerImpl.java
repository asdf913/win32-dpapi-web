package javax.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.MainServlet;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.LoggerUtil;

import io.github.toolfactory.narcissus.Narcissus;

public class ServletContextListenerImpl implements ServletContextListener {

	private static final Logger LOG = LoggerFactory.getLogger(ServletContextListenerImpl.class);

	private static class MethodVisitorImpl extends MethodVisitor {

		private Object value = null;

		private Collection<Object> values = null;

		private MethodVisitorImpl() {
			super(Opcodes.ASM9);
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
					&& !contains(values = ObjectUtils.getIfNull(values, ArrayList::new), value)) {
				//
				add(values, value);
				//
			} // if
				//
		}

		private static <E> void add(final Collection<E> instance, final E item) {
			if (instance != null) {
				instance.add(item);
			}
		}

		private static boolean contains(final Collection<?> instance, final Object item) {
			return instance != null && instance.contains(item);
		}

	}

	private Map<String, String> map = null;

	public ServletContextListenerImpl(final Map<String, String> map) {
		this.map = map;
	}

	@Override
	public void contextInitialized(final ServletContextEvent event) {
		//
		final Class<?> clz = MainServlet.class;
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
				final ServletContext servletContext = getServletContext(event);
				//
				String string = null;
				//
				for (int i = 0; i < IterableUtils.size(iterable); i++) {
					//
					setAttribute(servletContext, string = Objects.toString(IterableUtils.get(iterable, i)),
							MapUtils.getObject(map, string));
					//
				} // for
					//
			} // if
				//
		} catch (final IOException e) {
			//
			LoggerUtil.error(LOG, getMessage(e), e);
			//
		} // try
			//
	}

	private static String getMessage(final Throwable instance) {
		return instance != null ? instance.getMessage() : null;
	}

	private static ServletContext getServletContext(final ServletContextEvent instance) {
		return instance != null ? instance.getServletContext() : null;
	}

	private static void setAttribute(final ServletContext instance, final String name, final Object object) {
		if (instance != null) {
			instance.setAttribute(name, object);
		}
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

	private static Class<?> getClass(final Object instance) {
		return instance != null ? instance.getClass() : null;
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

	private static String getName(final Member instance) {
		return instance != null ? instance.getName() : null;
	}

	private static String getName(final Class<?> instance) {
		return instance != null ? instance.getName() : null;
	}

	@Override
	public void contextDestroyed(final ServletContextEvent event) {
	}

}