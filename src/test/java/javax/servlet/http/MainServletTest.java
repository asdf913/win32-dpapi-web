package javax.servlet.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.ServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.reflect.Reflection;

import io.github.toolfactory.narcissus.Narcissus;

class MainServletTest {

	private static class IH implements InvocationHandler {

		private PrintWriter writer;

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			//
			final String name = getName(method);
			//
			if (proxy instanceof ServletResponse && Objects.equals(name, "getWriter")) {
				//
				return writer;
				//
			} // if
				//
			throw new Throwable(name);
			//
		}

		private static String getName(final Member instance) {
			return instance != null ? instance.getName() : null;
		}

	}

	private MainServlet instance = null;

	private IH ih = null;

	@BeforeMethod
	void beforeMethod() {
		//
		instance = cast(MainServlet.class, Narcissus.allocateInstance(MainServlet.class));
		//
		ih = new IH();
		//
	}

	@Test
	void testNull() {
		//
		final Method[] ms = MainServlet.class.getDeclaredMethods();
		//
		Method m = null;
		//
		for (int i = 0; ms != null && i < ms.length; i++) {
			//
			if ((m = ArrayUtils.get(ms, i)) == null || m.isSynthetic()) {
				//
				continue;
				//
			} // if
				//
			Assert.assertNull(
					Narcissus.invokeMethod(instance, m, toArray(Collections.nCopies(m.getParameterCount(), null))),
					Objects.toString(m));
			//
		} // for
			//
	}

	@Test
	void testNotNull() {
		//
		final Method[] ms = MainServlet.class.getDeclaredMethods();
		//
		Method m = null;
		//
		Collection<Object> collection = null;
		//
		Class<?>[] parameterTypes = null;
		//
		Class<?> parameterType = null;
		//
		for (int i = 0; ms != null && i < ms.length; i++) {
			//
			if ((m = ArrayUtils.get(ms, i)) == null || m.isSynthetic()
					|| (parameterTypes = m.getParameterTypes()) == null) {
				//
				continue;
				//
			} // if
				//
			clear(collection = ObjectUtils.getIfNull(collection, ArrayList::new));
			//
			for (int j = 0; j < parameterTypes.length; j++) {
				//
				if ((parameterType = ArrayUtils.get(parameterTypes, j)) != null && parameterType.isInterface()) {
					//
					add(collection, Reflection.newProxy(parameterType, ih = ObjectUtils.getIfNull(ih, IH::new)));
					//
				} else {
					//
					add(collection, Narcissus.allocateInstance(parameterType));
					//
				} // if
					//
			} // for
				//
			Assert.assertNull(Narcissus.invokeMethod(instance, m, toArray(collection)), Objects.toString(m));
			//
		} // for
			//
	}

	private static <T> T cast(final Class<T> clz, final Object instance) {
		return clz != null && clz.isInstance(instance) ? clz.cast(instance) : null;
	}

	private static Object[] toArray(final Collection<?> instance) {
		return instance != null ? instance.toArray() : null;
	}

	private static <E> void add(final Collection<E> instance, final E item) {
		if (instance != null) {
			instance.add(item);
		}
	}

	private static void clear(final Collection<?> instance) {
		if (instance != null) {
			instance.clear();
		}
	}

	@Test
	public void testDoGet() throws ServletException, IOException {
		//
		if (instance == null) {
			//
			return;
			//
		} // if
			//
		try (final PrintWriter writer = new PrintWriter(new StringWriter())) {
			//
			if ((ih = ObjectUtils.getIfNull(ih, IH::new)) != null) {
				//
				ih.writer = writer;
				//
			} // if
				//
			instance.doGet(null, Reflection.newProxy(HttpServletResponse.class, ih));
			//
		} // try
			//
	}

}