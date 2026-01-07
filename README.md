
Autenticação Baseada em Credenciais (Usuário/Senha)
Autenticação Baseada em Tokens (JWT, OAuth 2.0)

Roles
Permissões

Para projetos arduino, o ideal é Basic Auth.

############
Geração da chave pública e privada:
openssl genrsa > app.key
openssl rsa -in app.key -pubout app.pub

https?

/*

	DATALOGGER sem relógio RTC, para missões de exploração:
	Se o wifi falhou no MCU, não pode dar new na data assim que o wifi voltar. Então, se passar a data, ou parte dela, criar uma data manualmente na api.
	MCU recebe ultima data em formato rfc3339
	
	com contador (DAQ com temporizador fixo) para cada evento medido
	com contador (DAQ sem temporizador fixo) => MCU registra no cartão SD o tempo aproximado de cada medição, a partir do clock do MCU ou módulo de RTC.
		Então apenas enviar cada evento aproximado assim que a reconexão for efetuada
		Se o tempo foi adquirido por clock do MCU, é possível que haja overflow
	*/
