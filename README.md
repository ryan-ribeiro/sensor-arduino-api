
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
