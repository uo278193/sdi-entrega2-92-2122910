const {ObjectId} = require("mongodb");
module.exports = function (app, usersRepository, messagesRepository) {

    app.get("/api/v1.0/sdigram", function (req, res) {
        try {
            let email = res.user.email;
            let filter = {email: email};
            let options = {};
            usersRepository.getUsers(filter, options).then(sdigram => {
                if (sdigram == null) {
                    res.status(404);
                    res.json({error: "Email inválido o no existe"})
                } else {
                    res.status(200);
                    res.json({sdigram: sdigram})
                }
            }).catch(error => {
                res.status(500);
                res.json({error: "Se ha producido un error al listar los usuarios"})
            });
        } catch (e) {
            res.status(500);
            res.json({error: "Se ha producido un error :" + e})
        }
    });
    app.post('/api/v1.0/messages/add/:id', function (req, res) {
        try {
            usersRepository.findUser({_id: ObjectId(req.params.id)},{}).then(receiver => {
                usersRepository.findUser({email: req.session.user},{}).then(sender => {
                    comprobar(sender, receiver, function(friends){
                        if(friends)  {
                            let message = {
                                emisor: req.session.user,
                                receptor: receiver.email,
                                texto: req.body.texto,
                                leido: false,
                                fecha: new Date()
                            }
                            messagesRepository.insertMessage(message, function (messageId) {
                                if (messageId == null) {
                                    res.status(409);
                                    res.json({error: "No se ha podido crear el mensaje. El recurso ya existe."});
                                } else {
                                    res.status(201);
                                    res.json({
                                        message: "Mensaje añadido correctamente.",
                                        _id: messageId
                                    })
                                }
                            });
                        } else{
                            res.status(401);
                            res.json({
                                error: "No es amigo del usuario, no se puede mandar el mensaje",
                                authenticated: false
                            })
                        }
                    })
                })
            })
        } catch (e) {
            res.status(500);
            res.json({error: "Se ha producido un error al intentar crear el mensaje: " + e})
        }
    });


    app.get("/api/v1.0/user/messages/:id", function (req, res) {
        try {
            let userEmail = req.session.user;
            let options = {};
            usersRepository.findUser({_id: ObjectId(req.params.id)}, options).then(user2 => {
                if (user2 == null) {
                    res.status(404);
                    res.json({error: "ID inválido o no existe"})
                } else {
                    let friendEmail = user2.email;
                    let filter = {receptor: userEmail, emisor: friendEmail};
                    messagesRepository.getMessages(filter, options).then(messages => {
                        if (messages == null) {
                            res.status(404);
                            res.json({error: "ID inválido o no existe"})
                        } else {
                            res.status(200);
                            let filter = {emisor: userEmail, receptor: friendEmail};
                            messagesRepository.getMessages(filter, options).then(messages2 => {
                                if (messages2 == null) {
                                    res.status(404);
                                    res.json({error: "ID inválido o no existe"})
                                } else {

                                    res.status(200);
                                    mensajesTotal = []
                                    messages.forEach(mensajes1 => {
                                        mensajesTotal.push(mensajes1)
                                    })
                                    messages2.forEach(mensajes2 => {
                                        mensajesTotal.push(mensajes2)
                                    })
                                    res.json({messages: mensajesTotal})
                                }
                            })
                        }
                    }).catch(error => {
                        res.status(500);
                        res.json({error: "Se ha producido un error al recuperar el mensaje: " + error})
                    });
                }
            }).catch (error => {
                res.status(500);
                res.json({error: "Se ha producido un error buscando al amigo :" + error})
            })
        }catch(e) {
            res.status(500);
            res.json({error: "Se ha producido un error recuperando los mensajes" + e})
        };
    });




    app.post('/api/v1.0/users/login', function (req, res) {
        try {
            let securePassword = app.get("crypto").createHmac('sha256', app.get('clave'))
                .update(req.body.password).digest("hex");
            if(req.body.email == null || req.body.email == "" ||
                req.body.password == null || req.body.password == "" ) {
                res.status(401); //Unauthorized
                res.json({
                    message: "Datos introducidos inválidos",
                    authenticated: false
                })
            }
            let filter = {
                email: req.body.email,
                password: securePassword
            }
            let options = {};
            usersRepository.findUser(filter, options).then(user => {
                if (user == null) {
                    res.status(401); //Unauthorized
                    res.json({
                        message: "Datos introducidos incorrectos",
                        authenticated: false
                    })
                } else {
                    req.session.user = user.email;
                    let token = app.get("jwt").sign(
                        {user: user.email, time: Date.now() / 1000}, "secreto"
                    );
                    res.status(200); //Authorized
                    res.json({
                        message: "Inicio de sesión correcto",
                        authenticated: true,
                        token: token
                    })
                }
            }).catch(error => {
                res.status(401);
                res.json({
                    error: "Se ha producido un error al verificar credenciales: " + error,
                    authenticated: false
                })
            })
        } catch (e) {
            res.status(500);
            res.json({
                error: "Se ha producido un error con la base de datos: " + e,
                authenticated: false
            })
        }
    });

    app.get("/api/v1.0/user/friends",async function (req, res) {
        let filter = {email: req.session.user};
        let options = { sort:{ nombre:1 }, projection:{ email:1, nombre:1, apellidos:1 } };
        usersRepository.findUser(filter, {}).then(user => {
            if (user == null) {
                res.status(404);
                res.json({error: "ID inválido o no existe"})
            } else {
                let friendsIds = user.friends;
                let filterFriendsIds = { _id: { $in: friendsIds} };
                usersRepository.findUsers(filterFriendsIds, options).then(friends =>{

                    getMessages(user, friends).then(completeFriends => {
                        res.status(200);
                        res.send({friends: completeFriends})
                    });

                }).catch(error => {
                    res.status(500);
                    res.json({error: "Se ha producido un error al listar los amigos: " + error})
                });
            }
        }).catch(error => {
            res.status(500);
            res.json({error: "Se ha producido un error al listar los amigos: " + error})
        });
    });


    app.get("/api/v1.0/messages", function (req, res) {
        try {
            let filter = {email: req.session.user};
            let options = {};
            messagesRepository.getMessages(filter, options).then(friend => {
                if (friend == null) {
                    res.status(404);
                    res.json({error: "ID inválido o no existe"})
                } else {
                    res.status(200);
                    res.json({friend: friend})
                }
            }).catch(error => {
                res.status(500);
                res.json({error: "Se ha producido un error al listar los mensajes: "+ error})
            });
        } catch (e) {
            res.status(500);
            res.json({error: "Se ha producido un error :" + e})
        }
    });




    app.put('/api/v1.0/messages/:id', function (req, res) {
        try {
            let messageId = ObjectId(req.params.id);
            let filter = {_id: messageId, receptor: req.session.user};
            const options = {$set: {"leido": true}};
            messagesRepository.updateMessage(filter, options).then(message => {
                if (message == null) {
                    res.status(404);
                    res.json({error: "ID inválido o no existe, no se ha actualizado el mensaje."});
                } else {
                    res.status(200);
                    res.json({
                        message: "Mensaje modificado correctamente."
                    })
                }
            }).catch(error => {
                res.status(500);
                res.json({error: "Se ha producido un error al modificar el mensaje: "+ error})
            });
        } catch (e) {
            res.status(500);
            res.json({error: "Se ha producido un error al intentar modificar el mensaje: " + e})
        }
    });





    function comprobar(user1, user2, callback){
        let filter = {_id: user1._id, friends: user2._id}
        usersRepository.findUser(filter, {}).then(user => {
            if(user == null)
                callback(false)
            else
                callback(true)
        }).catch(() => {
            callback(false);
        })
    }

    async function getMessages(user, friends){
        let completeFriends = [];
        let options = {sort:{ fecha: -1 }}
        for(let i = 0; i < friends.length; i++){
            let filter = {$or:[{emisor: user.email, receptor:friends[i].email}, {receptor: user.email, emisor:friends[i].email}]};
            await messagesRepository.getMessages(filter, options).then(messages => {
                completeFriends.push({user: friends[i], messages: messages});
            })
        }
        return completeFriends;
    }


}
