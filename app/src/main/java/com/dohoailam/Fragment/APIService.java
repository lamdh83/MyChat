package com.dohoailam.Fragment;

import com.dohoailam.Notifications.MyResponse;
import com.dohoailam.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAXIssRws:APA91bFfNXcQDnHLjoWpjXv-6AjWgxcpd_SIBl__TbmkuFv3zvP644YfwDc8pKtU65RaBRqFefAkzx1qQNuidCroMg_s4L9RFk9VNRT-2jYAKqlAPPNNJkmau2mC57cIpb-JRWq_aH9D"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
