package com.rest.test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.rest.testRest.BaseTest;
import com.rest.utils.DataUtils;

import io.restassured.RestAssured;
import io.restassured.specification.FilterableRequestSpecification;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)//executa os teste em ordem alfabetica
public class BarrigaTest extends BaseTest{
	
	private static String CONTA_NAME = "Conta" + System.nanoTime();//a cada execusao e pego o tempo e passado como conta para que nunca se repita 
	private static Integer CONTA_ID;
	private static Integer MOV_ID;
	
	@BeforeClass
	public void login() {
		Map<String, String> login = new HashMap<String, String>();
		login.put("emal", "");
		login.put("senha", "");
		
		String TOKEM = given()
			.body(login)
		.when()
			.post("/signin")
		.then()
			.statusCode(200)//atenção api mas antiga usa o JWT token, e as mas novas usa o bearer
			.extract().path("token");
		;
		RestAssured.requestSpecification.header("Authorization", "JWT " + TOKEM);
	}
	
	
	
	/**
	 * preciso criar uma conta para executar esse teste
	 */
	@Test
	public void t02_deveIncluirContaComSucesso() {
		CONTA_ID = given()
			//.header("Authorization", "JWT " + TOKEM)
			.body("{\"nome\":\""+CONTA_NAME+"\"}")
		.when()
			.post("/contas")
		.then()
			.statusCode(201)
			.extract().path("id")
		;
	}
	
	@Test
	public void t03_deveAlterarContaComSucesso() {
		given()
			//.header("Authorization", "JWT " + TOKEM)
			.body("{\"nome\":\""+CONTA_NAME+"conta alterada\"}")
			.param("id", CONTA_ID)
		.when()
			.put("/contas/{id}")//aula 56
		.then()
			.log().all()
			.statusCode(200)
			.body("nome", is(CONTA_NAME+"conta alterada"))
		;
	}
	@Test
	public void t04_naoDeveInserirContaComMesmoNome() {
		given()
			//.header("Authorization", "JWT " + TOKEM)
			.body("{\"nome\":\""+CONTA_NAME+"conta alterada\"}")
		.when()
			.put("/contas")
		.then()
			.log().all()
			.statusCode(400)
			.body("error", is("Já exite um conta com esse nome !"))
		;
	}
	
	@Test
	public void t05_deveInserirMovimentacaoComSucesso() {
		Movimentacao mov = getMovimentacaoValida();
		MOV_ID = given()
			//.header("Authorization", "JWT " + TOKEM)
			.body(mov)
		.when()
			.put("/transacoes")
		.then()
			.log().all()
			.statusCode(201)
			.extract().path("id")
		;
	}
	
	@Test
	public void t06_deveValidarCamposObrigatorioMovimentacao() {
		given()
			//.header("Authorization", "JWT " + TOKEM)
			.body("{}")
		.when()
			.put("/transacoes")
		.then()
			.log().all()
			.statusCode(400)
			.body("msg", hasItems(
					"Data da movimentação e obrigatório",
					"Data do pagamento e obrigatório",
					"Descrição e obrigatório",
					"Valor deve ser um número",
					"Conta e obrigatória",
					"Situação e obrigatório"
					))
		;
	}
	
	@Test
	public void t07_naoDeveInserirMovimentacaoComDataFutura() {
		Movimentacao mov = getMovimentacaoValida();
		mov.setData_transacao(DataUtils.getDataDiferencaDias(2));
		given()
			//.header("Authorization", "JWT " + TOKEM)
			.body(mov)
		.when()
			.put("/transacoes")
		.then()
			.statusCode(400)
			.body("$", hasSize(1))
			.body("msg", hasItem("Data da Movimentacao dave ser menor ou igual a data atual"))
		;
		
	}
	
	@Test
	public void t08_naoDeveRemoverContaComMovimentacao() {
		given()
			//.header("Authorization", "JWT " + TOKEM)
			.pathParam("id", CONTA_ID)
		.when()
			.delete("/contas/{id}")
		.then()
			.statusCode(500)
			.body("constraint", is("transacoes_conta_id_foreign"))
		;
		
	}
	
	@Test
	public void t_09deveCalcularSaldoContas() {
		given()
			//.header("Authorization", "JWT " + TOKEM)
		.when()
			.get("/saldo")
		.then()
			.statusCode(200)
			.body("find{it.conta_id == "+CONTA_ID+"}.saldo", is("100.00"))
		;
		
	}
	@Test
	public void t10_deveRemoverMovimentacao() {
		given()
			//.header("Authorization", "JWT " + TOKEM)
			.pathParam("id", MOV_ID)
		.when()
			.delete("/transacoes/{id}")//esse id nao e o mesmo do id da criação da conta
		.then()
			.statusCode(204)
		;
		
	}
	
	@Test
	public void t011_naoDeveAcessarAPISemTokem() {
		FilterableRequestSpecification req = (FilterableRequestSpecification) RestAssured.requestSpecification;
		req.removeHeader("Authorization");
		given()
		.when()
			.get("/contas")
		.then()
			.statusCode(401)
		;
	}
	private Movimentacao getMovimentacaoValida() {
		Movimentacao mov = new Movimentacao();
		mov.setConta_id(CONTA_ID);
		mov.setDescricao("Descricao da movimentacao");
		mov.setEnvolvido("Envolvdo mov");
		mov.setTipo("REC");
		mov.setData_transacao(DataUtils.getDataDiferencaDias(-1));
		mov.setData_pagamento(DataUtils.getDataDiferencaDias(5));
		mov.setValor(100f);
		mov.setStatus(true);
		return mov;
	}

}
