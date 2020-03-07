package dk.itu.moapd.realmdb

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class ActorName(
    @PrimaryKey var id: Int = 0,
    @Required var name: String = ""
) : RealmObject()
