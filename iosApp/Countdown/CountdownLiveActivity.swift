//
//  CountdownLiveActivity.swift
//  Countdown
//
//  Created by E 9 on 12/11/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import ActivityKit
import WidgetKit
import SwiftUI

//public struct CountdownAttributes: ActivityAttributes {
//    public struct ContentState: Codable, Hashable {
//        // Dynamic stateful properties about your activity go here!
//        
//    }
//
//    // Fixed non-changing properties about your activity go here!
//    var startTime: Date
//    var endTime: Date
//}

struct CountdownLiveActivity: Widget {
    var body: some WidgetConfiguration {
        ActivityConfiguration(for: CountdownAttributes.self) { context in
            // Lock screen/banner UI goes here
            VStack() {
                HStack(alignment: .top, spacing: 16) {
                    VStack(alignment: .leading) {
                        Text(context.attributes.className)
                            .font(.body)
                            .foregroundStyle(.white)
                        
                        Text("Ends at: " + formatDateToHoursAndMinutes(date: context.attributes.endTime))
                            .font(.caption)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    
                    
                
                    VStack(alignment: .trailing) {
                        Text("Time Left: ")
                            .font(.caption)
                        
                        Text(timerInterval: context.attributes.startTime...context.attributes.endTime, countsDown: true, showsHours: true)
                            .font(.body)
                            .foregroundStyle(.white)
                            .multilineTextAlignment(.trailing)
                        
                    }
                    .frame(maxWidth: .infinity, alignment: .trailing)
                }

                ProgressView(timerInterval: context.attributes.startTime...context.attributes.endTime, countsDown: false, label: { Text("") }, currentValueLabel: { Text("") })
                    .progressViewStyle(LinearProgressViewStyle())
                    .frame(height: 20)
                    .scaleEffect(x: 1, y: 1.5, anchor: .center)

            }
            .frame(maxWidth: .infinity)
            .padding()
            .activityBackgroundTint(.black.opacity(0.25))

        } dynamicIsland: { context in
            DynamicIsland {
                // Expanded UI goes here.  Compose the expanded UI through
                // various regions, like leading/trailing/center/bottom
                DynamicIslandExpandedRegion(.leading) {
                    Text(context.attributes.className)
                        .font(.body)
                }
                DynamicIslandExpandedRegion(.trailing) {
                    Text(timerInterval: context.attributes.startTime...context.attributes.endTime, countsDown: true, showsHours: true)
                        .font(.body)
                        .multilineTextAlignment(.trailing)
                }
                DynamicIslandExpandedRegion(.bottom) {
                    ProgressView(timerInterval: context.attributes.startTime...context.attributes.endTime, countsDown: false, label: { Text("") }, currentValueLabel: { Text("") })
                        .progressViewStyle(LinearProgressViewStyle())
                        .frame(height: 20)
                        .scaleEffect(x: 1, y: 1.5, anchor: .center)
                }
            } compactLeading: {
                Text(context.attributes.className)
                    .font(.caption)
                    .padding(14)
            } compactTrailing: {
                Text(timerInterval: context.attributes.startTime...context.attributes.endTime, countsDown: true, showsHours: true)
                    .font(.body)
                    .multilineTextAlignment(.trailing)
                    .padding(14)
            } minimal: {
                ProgressView(timerInterval: context.attributes.startTime...context.attributes.endTime, countsDown: false, label: { Text("") }, currentValueLabel: { Text("") })
                    .progressViewStyle(CircularProgressViewStyle())
                    .frame(height: 20)
                    .scaleEffect(x: 1, y: 1, anchor: .center)
            }
            .widgetURL(URL(string: "http://www.apple.com"))
            .keylineTint(Color.red)
        }
    }
    
    func formatDateToHoursAndMinutes(date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        return formatter.string(from: date)
    }
}

