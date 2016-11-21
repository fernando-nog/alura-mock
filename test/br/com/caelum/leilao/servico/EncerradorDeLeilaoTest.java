package br.com.caelum.leilao.servico;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.inOrder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.infra.dao.EnviadorDeEmail;
import br.com.caelum.leilao.infra.dao.LeilaoDao;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;

public class EncerradorDeLeilaoTest {
	
	private RepositorioDeLeiloes leilaoDao;
	private EnviadorDeEmail carteiro;
	Calendar dataCalendar = Calendar.getInstance();
	
	@Before
	public void setup(){
		RepositorioDeLeiloes leilaoDao = mock(LeilaoDao.class);
		EnviadorDeEmail carteiro = mock(EnviadorDeEmail.class);
	}
	
	
	@Test
	public void deveEncerrarLeiloesQueComeraramUmaSemanaAntes(){

		dataCalendar.set(1999, 1, 20);
		
		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(dataCalendar).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(dataCalendar).constroi();
		List<Leilao> leiloesAntigos = Arrays.asList(leilao1, leilao2);
		
		when(leilaoDao.correntes()).thenReturn(leiloesAntigos);
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(leilaoDao, carteiro);
		encerrador.encerra();
		
		assertEquals(2, encerrador.getTotalEncerrados());
		assertTrue(leilao1.isEncerrado());
		assertTrue(leilao2.isEncerrado());
	}
	
	@Test
    public void naoDeveEncerrarLeiloesQueComecaramMenosDeUmaSemanaAtras() {

        dataCalendar.add(Calendar.DAY_OF_MONTH, -1);

        Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma")
            .naData(dataCalendar).constroi();
        Leilao leilao2 = new CriadorDeLeilao().para("Geladeira")
            .naData(dataCalendar).constroi();

        when(leilaoDao.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));

        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(leilaoDao,carteiro);
        encerrador.encerra();

        assertEquals(0, encerrador.getTotalEncerrados());
        assertFalse(leilao1.isEncerrado());
        assertFalse(leilao2.isEncerrado());
        verify(leilaoDao, never()).atualiza(leilao1);
        verify(leilaoDao, never()).atualiza(leilao2); 
    }
	
	@Test
    public void naoDeveEncerrarLeiloesCasoNaoHajaNenhum() {

        when(leilaoDao.correntes()).thenReturn(new ArrayList<Leilao>());

        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(leilaoDao, carteiro);
        encerrador.encerra();

        assertEquals(0, encerrador.getTotalEncerrados());
    }
	
	@Test
	public void deveAtualizarLeiloesEncerrados(){
		dataCalendar.set(1999, 1, 20);
		
		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(dataCalendar).constroi();
		
		when(leilaoDao.correntes()).thenReturn(Arrays.asList(leilao1));
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(leilaoDao, carteiro);
		encerrador.encerra();
		
		verify(leilaoDao, times(1)).atualiza(leilao1);
	}
	
	@Test
    public void deveEnviarEmailAposPersistirLeilaoEncerrado() {
        dataCalendar.set(1999, 1, 20);

        Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(dataCalendar).constroi();

        when(leilaoDao.correntes()).thenReturn(Arrays.asList(leilao1));

        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(leilaoDao, carteiro);

        encerrador.encerra();

        InOrder inOrder = inOrder(leilaoDao, carteiro);
        inOrder.verify(leilaoDao, times(1)).atualiza(leilao1);    
        inOrder.verify(carteiro, times(1)).envia(leilao1);    
    }
}
