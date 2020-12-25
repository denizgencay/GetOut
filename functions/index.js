const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

exports.updateSenderChats = functions.firestore.document('Chats/{chatId}/messages/{messageId}').onCreate((change,context) => {
    var chatId = context.params.chatId;
    var senderId = change.data().senderUid;
    var receiverId = change.data().receiverUid;
 
    return admin.firestore().collection('Chats/'+chatId+'/messages').get().then(response => {
        if (response.size === 1) {
            return admin.firestore().doc('Users/'+senderId).get().then(response2 => {
                var chats = response2.data().chats
                chats.push(chatId)
                return admin.firestore().doc('Users/'+senderId).update({chats:chats}).then(response3 => {
                    return admin.firestore().doc('Users/'+receiverId).get().then(response4 => {
                        var chats2 = response4.data().chats
                        chats2.push(chatId)
                        return admin.firestore().doc('Users/'+receiverId).update({chats:chats2})
                    })
                })
            })
        } else {
           return admin.firestore().doc('Users/'+senderId).get().then(response2 => {
               var chats = response2.data().chats
               chats.splice(chats.indexOf(chatId),1)
               chats.push(chatId)          
               return admin.firestore().doc('Users/'+senderId).update({chats:chats}).then(response3 => {
                return admin.firestore().doc('Users/'+receiverId).get().then(response4 => {
                    var chats2 = response4.data().chats
                    chats2.splice(chats2.indexOf(chatId),1)
                    chats2.push(chatId)  
                    return admin.firestore().doc('Users/'+receiverId).update({chats:chats2})
                })
            })
           })
        }
    })    
});

// exports.deleteInstantInvite = functions.pubsub.schedule('* * * * *').onRun(context => {
//     return admin.firestore().collection('instantInvites').get().then(response => {
//         return response.forEach(snapshot => {
//             var expireDate = snapshot.data().expireDate
//             var expireInMillies = expireDate._seconds * 1000
//             var now = new Date()
//             var nowInMillies = now.getTime()
//             var inviteId = snapshot.id
//             var ownerId = snapshot.data().userUid

//             if (nowInMillies >= expireInMillies) {
//                 return admin.firestore().doc('Users/'+ownerId).get().then(response3 => {
//                     var instantInviteUids = response3.data().instantInviteUids
//                     instantInviteUids.splice(instantInviteUids.indexOf(inviteId),1)
//                     var activeInstantInvite
//                     if (instantInviteUids.length === 0) {
//                         activeInstantInvite = false
//                     } else {
//                         activeInstantInvite = true
//                     }                
//                     return admin.firestore().doc('Users/'+ownerId).update({instantInviteUids:instantInviteUids,activeInstantInvite:activeInstantInvite}).then(response4 => {
//                         return admin.firestore().doc('instantInvites/'+inviteId).delete().then(response5 => {
//                             var attendants = snapshot.data().attendants
//                             if (attendants !== null) {
//                                 for (i = 0 ; i < attendants.length ; i++) {
//                                     return admin.firestore().doc('Users/'+attendants[i]).get().then(response2 => {
//                                         var attendantInstantInvites = response2.data().attendantInstantInviteUids
//                                         if (attendantInstantInvites !== null) {
//                                             if (attendantInstantInvites.length > 0) {
//                                                 attendantInstantInvites.splice(attendantInstantInvites.indexOf(inviteId),1)
//                                                 return admin.firestore().doc('Users/'+attendants[i]).update({attendantInstantInviteUids:attendantInstantInvites})
//                                             } else {
//                                                 return null
//                                             }
//                                         } else {
//                                             return null
//                                         }
//                                     })
//                                }
//                                return console.log('success')
//                             } else {
//                                 return null
//                             }            
//                         })
//                     })
//                 })     
//             }
//         })
//     })
// })

// exports.deletePlannedInvite = functions.pubsub.schedule('* * * * *').onRun(context => {
//     return admin.firestore().collection('plannedInvites').get().then(response => {
//         return response.forEach(snapshot => {
//             var expireDate = snapshot.data().expireDate
//             var expireInMillies = expireDate._seconds * 1000
//             var now = new Date()
//             var nowInMillies = now.getTime()
//             var inviteId = snapshot.id
//             var ownerId = snapshot.data().userUid

//             if (nowInMillies >= expireInMillies) {
//                 return admin.firestore().doc('Users/'+ownerId).get().then(response3 => {
//                     var plannedInviteUids = response3.data().plannedInviteUids
//                     plannedInviteUids.splice(plannedInviteUids.indexOf(inviteId),1)
//                     var activePlannedInvite
//                     if (plannedInviteUids.length === 0) {
//                         activePlannedInvite = false
//                     } else {
//                         activePlannedInvite = true
//                     }                
//                     return admin.firestore().doc('Users/'+ownerId).update({plannedInviteUids:plannedInviteUids,activePlannedInvite:activePlannedInvite}).then(response4 => {
//                         return admin.firestore().doc('plannedInvites/'+inviteId).delete().then(response5 => {
//                             var attendants = snapshot.data().attendants
//                             if (attendants !== null) {
//                                 for (i = 0 ; i < attendants.length ; i++) {
//                                     return admin.firestore().doc('Users/'+attendants[i]).get().then(response2 => {
//                                         var attendantPlannedInviteUids = response2.data().attendantPlannedInviteUids
//                                         if (attendantPlannedInviteUids !== null) {
//                                             if (attendantPlannedInviteUids.length > 0) {
//                                                 attendantPlannedInviteUids.splice(attendantPlannedInviteUids.indexOf(inviteId),1)
//                                                 return admin.firestore().doc('Users/'+attendants[i]).update({attendantPlannedInviteUids:attendantPlannedInviteUids})
//                                             } else {
//                                                 return null
//                                             }
//                                         } else {
//                                             return null
//                                         }
//                                     })
//                                }
//                                return console.log('success')
//                             } else {
//                                 return null
//                             }            
//                         })
//                     })
//                 })     
//             }
//         })
//     })
// })

// exports.deleteEvent = functions.pubsub.schedule('* * * * *').onRun(context => {
//     return admin.firestore().collection('events').get().then(response => {
//         return response.forEach(snapshot => {
//             var expireDate = snapshot.data().expireDate
//             var expireInMillies = expireDate._seconds * 1000
//             var now = new Date()
//             var nowInMillies = now.getTime()
//             var inviteId = snapshot.id
//             var ownerId = snapshot.data().userUid

//             if (nowInMillies >= expireInMillies) {
//                 return admin.firestore().doc('Users/'+ownerId).get().then(response3 => {
//                     var eventUids = response3.data().eventUids
//                     eventUids.splice(eventUids.indexOf(inviteId),1)
//                     var activeEvent
//                     if (eventUids.length === 0) {
//                         activeEvent = false
//                     } else {
//                         activeEvent = true
//                     }                
//                     return admin.firestore().doc('Users/'+ownerId).update({eventUids:eventUids,activeEvent:activeEvent}).then(response4 => {
//                         return admin.firestore().doc('events/'+inviteId).delete().then(response5 => {
//                             var attendants = snapshot.data().attendants
//                             if (attendants !== null) {
//                                 for (i = 0 ; i < attendants.length ; i++) {
//                                     return admin.firestore().doc('Users/'+attendants[i]).get().then(response2 => {
//                                         var attendantEventUids = response2.data().attendantEventUids
//                                         if (attendantEventUids !== null) {
//                                             if (attendantEventUids.length > 0) {
//                                                 attendantEventUids.splice(attendantEventUids.indexOf(inviteId),1)
//                                                 return admin.firestore().doc('Users/'+attendants[i]).update({attendantEventUids:attendantEventUids})
//                                             } else {
//                                                 return null
//                                             }
//                                         } else {
//                                             return null
//                                         }
//                                     })
//                                }
//                                return console.log('success')
//                             } else {
//                                 return null
//                             }            
//                         })
//                     })
//                 })     
//             }
//         })
//     })
// })


// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });
