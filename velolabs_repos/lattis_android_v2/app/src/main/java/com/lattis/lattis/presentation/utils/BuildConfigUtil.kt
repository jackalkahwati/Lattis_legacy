package com.lattis.lattis.presentation.utils

import io.lattis.lattis.BuildConfig

object BuildConfigUtil {

    fun applicationName():String{
        return when(BuildConfig.FLAVOR_product.lowercase()){
            "lattis"->{ "Lattis"}
            "guestbike"->{ "Guestbike"}
            "velotransit" -> { "VeloTransit"}
            "sandypedals" -> { "Sandy Pedals"}
            "giraff" -> { "Giraff"}
            "goscoot" -> { "GoScoot"}
            "grin" -> {"Grin Lima"}
            "grinsantiago" -> {"Grin Santiago"}
            "wave" -> {"Wave Co."}
            "wawe" -> {"WAWE Mobility"}
            "mount" -> {"Mount"}
            "unlimitedbiking" -> {"Unlimited Biking Micromobility"}
            "monkeydonkey" -> {"Monkey Donkey"}
            "bandwagon" -> {"Bandwagon"}
            "ourbike" -> {"OurBike"}
            "fin" -> {"Fin"}
            "hooba" -> {"Hooba"}
            "blade" -> {"Blade"}
            "pacific" -> {"Pacific Ride+Share"}
            "trip" -> {"TRIP"}
            "greenriders" -> {"Green-Riders"}
            "twowheelrental" -> {"TWR - Serving The Explorer"}
            "rockvelo" -> {"RockVelo"}
            "falcosmart" -> {"FalcoSmart"}
            "thriveryde" -> {"Thrive Ryde"}
            "lockem" -> {"LOCKEM"}
            "robyn" ->{"Robyn Scooters"}
            "yeti" ->{"Yeti Rides - Scooters"}
            "overwatt" -> {"Over Watt"}
            "wbs" -> {"WINTERSTEIGER"}
             else ->{"Lattis"}
        }
    }

    fun privacyPolicy():String{
        return when(BuildConfig.FLAVOR_product.lowercase()){
            "lattis"->{"https://lattis.io/pages/privacy-policy"}
            "guestbike"->{"https://guestbike.com/disclaimer/"}
            "velotransit" -> {"https://lattis.io/pages/privacy-policy"}
            "sandypedals" -> {"https://sandypedalsbikes.com/privacy"}
            "giraff" -> {"https://lattis.io/pages/privacy-policy"}
            "goscoot" -> {"https://secureservercdn.net/50.62.195.83/377.960.myftpupload.com/wp-content/uploads/2021/05/go-scoot-Privacy-Policy_210503.pdf"}
            "grin" -> {"https://content.grow.mobi/p/pe/privacy"}
            "grinsantiago" -> {"https://content.grow.mobi/p/cl/privacy"}
            "wave" -> {"https://waveco.limited/PrivacyPolicy"}
            "wawe" -> {"https://www.iubenda.com/privacy-policy/92118527"}
            "mount" -> {"https://mountlocks.com/assets/Mount_Privacy_Policy.pdf"}
            "bandwagon" -> {"https://bandwagmag.com/privacy-policy-for-bandwagon-scooters/"}
            "monkeydonkey" -> {"https://monkeydonkey.bike/privacy"}
            "unlimitedbiking" -> {"https://www.unlimitedbiking.com/micromobility-privacy-policy/"}
            "ourbike"->{"https://ourbike.co.uk/assets/uploads/files/privacy-policy.pdf"}
            "fin"->{"https://findf.in/privacy-policy/"}
            "hooba"->{"http://hooba.eu/en/privacy-policy/"}
            "blade"->{"https://www.blade.me/privacy/"}
            "pacific"->{"https://pacificrides.io/privacy-policy/"}
            "trip"->{"https://www.ridetrip.com/privacy-policy"}
            "greenriders"->{"https://green-riders.fr/content/2-mentions-legales"}
            "twowheelrental"->{"https://38da5e79-712f-46d4-807c-bbb7b2aceaeb.filesusr.com/ugd/0078b7_fd2f116544334f0b8fda38d432ba0e9e.docx?dn=White%20Label%20Privacy%20Policy%20Template.DOCX"}
            "rockvelo" -> {"https://www.rockvelo.com/terms-and-conditions/"}
            "falcosmart" -> {"https://rentals.falco.co.uk/privacy.html"}
            "thriveryde" -> {"https://thriveryde.com/terms/"}
            "lockem" -> {"https://cdn.website-editor.net/s/6852d6c357704495ae92740468821bce/files/uploaded/Privacy%2520Policy.pdf?Expires=1664112808&Signature=Zsm4j6PXfL-zOtH-64rZWU0fFBAwCH8-vTPfhjc7lcfiyvRHY8ir8Mfdu81AvYyCU72UzGurYWqy4lrW~GGBDhpjhaHUdhJkmt9FF3yddK14Ke80K69ZZ5AtGlyclDMhhEjC3OScVhP8dfOU7lcA~P-H6PaFLWCzg0AOscWnsighkPY~B1Z00UqV5AYIc3G4F-QZIdl0VqOSD7mhlrgnLOeiIlBVqPYL5-gAjuoF5qIQaAebkqyq3BjBC0t79jq78HaSXjr8f30Q-z8YcQALgTwzUuU7hxsKcSX51wN~OtXpxvfb61slzpz9ef7zMLaBRJX6lM43zNQrz1ux9Kb-qA__&Key-Pair-Id=K2NXBXLF010TJW"}
            "robyn" -> {"https://www.robynscooters.com/_files/ugd/f3c63c_7547dfc9b3e3419e9075e83bafd9b1bf.pdf"}
            "yeti" -> {"https://yetiscooters.com/privacy-policy"}
            else ->{ "https://lattis.io/pages/privacy-policy"}
        }
    }

    fun termsOfService():String{
        return when(BuildConfig.FLAVOR_product.lowercase()){
            "lattis"->{"https://lattis.io/pages/terms-of-use"}
            "guestbike"->{"https://guestbike.com/disclaimer/"}
            "velotransit" -> {"https://lattis.io/pages/terms-of-use"}
            "sandypedals" -> {"https://sandypedalsbikes.com/terms"}
            "giraff" -> {"https://lattis.io/pages/terms-of-use"}
            "goscoot" -> {"https://secureservercdn.net/50.62.195.83/377.960.myftpupload.com/wp-content/uploads/2021/05/GO-SCOOT-TERMS-AND-CONDITION-APP-May-3-2021.pdf"}
            "grin" -> {"https://content.grow.mobi/p/pe/terms"}
            "grinsantiago" -> {"https://content.grow.mobi/p/cl/terms"}
            "wave" -> {"https://waveco.limited/PrivacyPolicy"}
            "wawe" -> {"https://www.iubenda.com/termini-e-condizioni/92118527"}
            "mount" -> {"https://mountlocks.com/assets/Mount_Terms_and_Conditions_Final_2.23.pdf"}
            "bandwagon" -> {"https://bandwagmag.com/terms-of-use/"}
            "monkeydonkey" -> {"https://monkeydonkey.bike/tos"}
            "unlimitedbiking" -> {"https://www.unlimitedbiking.com/micromobility-terms-conditions/"}
            "ourbike"->{"https://ourbike.co.uk/assets/uploads/files/terms-and-conditions.pdf"}
            "fin"->{"https://findf.in/rental-agreement/"}
            "hooba"->{"https://hooba.eu/en/terms-and-conditions/"}
            "blade"->{"https://www.blade.me/terms-conditions"}
            "pacific"->{"https://pacificrides.io/terms-conditions/"}
            "trip"->{"https://www.ridetrip.com/terms-of-service"}
            "greenriders"->{"https://green-riders.fr/content/2-mentions-legales"}
            "twowheelrental"->{"https://38da5e79-712f-46d4-807c-bbb7b2aceaeb.filesusr.com/ugd/0078b7_0d2b4bea0a6c4682aeaf17effff34195.pdf"}
            "rockvelo" -> {"https://www.rockvelo.com/terms-and-conditions/"}
            "falcosmart" -> {"https://rentals.falco.co.uk/terms-and-conditions.html"}
            "thriveryde" -> {"https://thriveryde.com/terms/"}
            "lockem" -> {"https://cdn.website-editor.net/s/6852d6c357704495ae92740468821bce/files/uploaded/Terms%2520and%2520Conditions.pdf?Expires=1664112808&Signature=JXNEx2gZgTLiEV-TPtBGapBIYxDrcb4OJ-oqfzY9ZAWpGgInbeu0ooWob8Dvf4g3FVQwxErD7~oy1HQ5zI-8jn1Z0CJCKw--S-wxkIlpz2iiib0HUsd00ZFektW36q-YautKemVxa3E30ANqLmj8zHDLwZ2sdnMav3MgmV7b~ILUk5k1tlkDFw1PHqZj2GHglp54xkCudXgMgT9g3pTGBO1kaiAOW5Ns029qrjikC4ifagjwaLl54DH2wj-jSa9xRUegp2qzwp1fRBeaU-5sTxcyDkpyMVnGmQ3ME~bXKPGFzfa4vlT0Jfcf-AsxheHXXORTgzrf2HWDF0goXUq9HQ__&Key-Pair-Id=K2NXBXLF010TJW"}
            "robyn" -> {"https://www.robynscooters.com/_files/ugd/f3c63c_7547dfc9b3e3419e9075e83bafd9b1bf.pdf"}
            "yeti" -> {"https://yetiscooters.com/terms-of-service"}
            else -> { "https://lattis.io/pages/terms-of-use"}
        }
    }

}