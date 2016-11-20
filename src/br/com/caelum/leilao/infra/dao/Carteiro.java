package br.com.caelum.leilao.infra.dao;

import br.com.caelum.leilao.dominio.Leilao;

public class Carteiro implements EnviadorDeEmail {

	public void envia(Leilao leilao) {
		System.out.println("Envia emgail para ");
	}

}
