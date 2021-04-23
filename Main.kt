package org.tat.plugin


import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.PluginData
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.contact.isAdministrator
import net.mamoe.mirai.contact.isOwner
import net.mamoe.mirai.data.*
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.*
import kotlin.Exception
import kotlin.random.Random

object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "org.tat.plugin",
        name = "TATPlugin",
        version = "1.0.0"
    )
) {

    override fun onEnable() {
        super.onEnable()
        Event()
    }

    private fun GetFromURL(url: String): String? {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url).build()
        val response = client.newCall(request).execute()
        val body = response.body?.string()
        return body
    }

    private fun Event() {
        val AdminGroup = 442088315

        GlobalEventChannel.subscribeAlways<MemberJoinRequestEvent> {
            /**
             * 自动处理加群验证
             */
            val mess = message.replace("\\s".toRegex(), "")
            if (groupId.toInt() == 858751734 || groupId.toInt() == 1138752314 || groupId.toInt() == 197138076) {
                if (mess.indexOf("pvzhome.com", 0) != -1 || mess.indexOf("pvzchina", 0) != -1 || mess.indexOf("booen", 0) != -1) {
                    accept()
                } else {
                    reject()
                }
            }
        }

        GlobalEventChannel.subscribeAlways<GroupMessageEvent> {
            /**
             * val obj = JSONObject.parseObject(body)
             * sender.sendMessage(obj.getJSONArray("newslist").getJSONObject(0).getString("con"))
             */
            val strList = message.content.split(' ')
            /**
             * 观测系统：@全体
             */
            if (message.toString().indexOf("[mirai:atall]") != -1) {
                val mess = message.content.replace("@全体成员", "")
                bot.getGroup(442088315)?.sendMessage("[观测系统]${senderName}(${sender.id})在群${group.name}(${group.id})发送了一条@全体消息，内容为\n${mess}")
            }

            /**
             * 管理功能
             */
            if (group.id.toInt() == AdminGroup) {
                /**
                 * 通知
                 */
                if (strList[0] == "通知") {
                    var mess = ""
                    for (str in strList) {
                        if (str != "通知") {
                            mess += str
                            mess += " "
                        }
                    }

                    var alreadyList: Array<Long> = emptyArray()
                    for (Group in bot.groups) {
                        if (Group.id != group.id && (Group.botPermission.isAdministrator() || Group.botPermission.isOwner()) && alreadyList.indexOf(Group.id) == -1) {
                            alreadyList += Group.id
                            Group.sendMessage(AtAll + "官方通知:\n" + mess)
                        }
                    }
                }

                /**
                 * 禁言全体
                 */
                if (strList[0] == "禁言全体"){
                    var ok = true

                    try {
                        bot.getGroup(strList[1].toLong())?.settings?.isMuteAll = true
                    }catch (e: Exception) {
                        group.sendMessage("禁言失败！遭遇错误！")
                        ok = false
                    }finally {
                        if (ok && bot.getGroup(strList[1].toLong()) != null)
                            group.sendMessage("禁言操作已完成。")
                        else
                            group.sendMessage("禁言失败！遭遇错误！")
                    }
                }
            }
        }

        GlobalEventChannel.subscribeAlways<MemberMuteEvent> {
            /**
             * 同步禁言
             */
            for (Group in bot.groups) {
                if (operator != null && Group.botPermission.isAdministrator() && Group.id != group.id)
                    Group.getMember(member.id)?.mute(durationSeconds)
            }
        }

        GlobalEventChannel.subscribeAlways<MemberUnmuteEvent> {
            /**
             * 同步解禁
             */
            for (Group in bot.groups) {
                if (operator != null && Group.botPermission.isAdministrator() && Group.id != group.id) {
                    Group.getMember(member.id)?.unmute()
                }
            }
        }

        GlobalEventChannel.subscribeAlways<GroupMuteAllEvent> {
            /**
             * 全体禁言检测
             */
            if (operator != null && new) {
                group.settings.isMuteAll = false
                group.sendMessage("检测到非法禁言全体操作，已自动解除，请通过机器人后台进行全体禁言")
            }
        }
    }
}
