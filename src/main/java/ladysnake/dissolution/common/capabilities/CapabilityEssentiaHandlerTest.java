package ladysnake.dissolution.common.capabilities;

import static org.junit.Assert.*;

import org.junit.Test;

import ladysnake.dissolution.api.EssentiaTypes;
import ladysnake.dissolution.api.EssentiaStack;
import ladysnake.dissolution.api.IEssentiaHandler;

public class CapabilityEssentiaHandlerTest {

	@Test
	public void testInsertion1() {
		IEssentiaHandler handler = new CapabilityEssentiaHandler.DefaultEssentiaHandler(5);
		assertEquals(handler.insert(new EssentiaStack(EssentiaTypes.METALLIS, 7)), new EssentiaStack(EssentiaTypes.METALLIS, 2));
	}
	
	@Test
	public void testInsertion2() {
		IEssentiaHandler handler = new CapabilityEssentiaHandler.DefaultEssentiaHandler(5);
		assertTrue(handler.insert(new EssentiaStack(EssentiaTypes.METALLIS, 5)).isEmpty());
	}
	
	@Test
	public void testInsertion3() {
		IEssentiaHandler handler = new CapabilityEssentiaHandler.DefaultEssentiaHandler(5);
		handler.insert(new EssentiaStack(EssentiaTypes.METALLIS, 3));
		assertEquals(new EssentiaStack(EssentiaTypes.METALLIS, 1), handler.insert(new EssentiaStack(EssentiaTypes.METALLIS, 3)));
	}
	
	@Test
	public void testInsertion4() {
		IEssentiaHandler handler = new CapabilityEssentiaHandler.DefaultEssentiaHandler(5);
		assertEquals(EssentiaStack.EMPTY, handler.insert(EssentiaStack.EMPTY));
	}
	
	@Test
	public void testInsertion5() {
		IEssentiaHandler handler = new CapabilityEssentiaHandler.DefaultEssentiaHandler(5);
		handler.insert(new EssentiaStack(EssentiaTypes.METALLIS, 2));
		EssentiaStack oracle = new EssentiaStack(EssentiaTypes.CINNABARIS, 5);
		assertEquals(oracle, handler.insert(oracle));
	}
	
	@Test
	public void testInsertion6() {
		IEssentiaHandler handler = new CapabilityEssentiaHandler.DefaultEssentiaHandler(5);
		handler.insert(new EssentiaStack(EssentiaTypes.METALLIS, 2));
		assertTrue(handler.insert(new EssentiaStack(EssentiaTypes.METALLIS, 2)).isEmpty());
	}
	
	@Test
	public void testExtraction1() {
		IEssentiaHandler handler = new CapabilityEssentiaHandler.DefaultEssentiaHandler(5);
		assertTrue(handler.extract(5).isEmpty());
	}
	
	@Test
	public void testExtraction2() {
		IEssentiaHandler handler = new CapabilityEssentiaHandler.DefaultEssentiaHandler(5);
		EssentiaStack oracle = new EssentiaStack(EssentiaTypes.CINNABARIS, 2);
		handler.insert(oracle);
		assertEquals(oracle, handler.extract(2));
	}
	
	@Test
	public void testExtraction3() {
		IEssentiaHandler handler = new CapabilityEssentiaHandler.DefaultEssentiaHandler(5);
		EssentiaStack oracle = new EssentiaStack(EssentiaTypes.CINNABARIS, 1);
		handler.insert(oracle);
		assertEquals(oracle, handler.extract(3));
	}
	
	@Test
	public void testFlow1() {
		IEssentiaHandler handler = new CapabilityEssentiaHandler.DefaultEssentiaHandler(5);
		handler.setSuction(3, EssentiaTypes.CINNABARIS);
		IEssentiaHandler handler2 = new CapabilityEssentiaHandler.DefaultEssentiaHandler(3);
		handler2.setSuction(4, EssentiaTypes.UNTYPED);
		EssentiaStack oracle = new EssentiaStack(EssentiaTypes.CINNABARIS, 1);
		handler.insert(oracle);
		handler.flow(handler2);
		assertEquals(oracle, handler2.extract(3));
	}
	
	@Test
	public void testFlow2() {
		IEssentiaHandler handler = new CapabilityEssentiaHandler.DefaultEssentiaHandler(5);
		handler.setSuction(3, EssentiaTypes.CINNABARIS);
		IEssentiaHandler handler2 = new CapabilityEssentiaHandler.DefaultEssentiaHandler(3);
		handler2.setSuction(4, EssentiaTypes.METALLIS);
		EssentiaStack oracle = new EssentiaStack(EssentiaTypes.CINNABARIS, 1);
		handler.insert(oracle);
		handler.flow(handler2);
		assertTrue(handler2.extract(3).isEmpty());
	}
	
	@Test
	public void testFlow3() {
		IEssentiaHandler handler = new CapabilityEssentiaHandler.DefaultEssentiaHandler(5);
		handler.setSuction(3, EssentiaTypes.CINNABARIS);
		IEssentiaHandler handler2 = new CapabilityEssentiaHandler.DefaultEssentiaHandler(3);
		handler2.setSuction(2, EssentiaTypes.UNTYPED);
		EssentiaStack oracle = new EssentiaStack(EssentiaTypes.CINNABARIS, 1);
		handler.insert(oracle);
		handler.flow(handler2);
		assertTrue(handler2.extract(3).isEmpty());
	}
	

}
