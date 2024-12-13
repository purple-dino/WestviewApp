import SwiftUI
import ActivityKit
import WestVUE


@main
struct iOSApp: App {
    init() {
        Notifications_iosKt.createLiveActivity = {
            
            
            if #available(iOS 16.2, *) {
                for activity in Activity<CountdownAttributes>.activities {
                  Task {
                    await activity.end(nil, dismissalPolicy: .immediate)
                  }
                }
            }
            
            let attributes = CountdownAttributes(startTime: Date().advanced(by: TimeInterval(SchedulePageKt.startTime)), endTime: Date().advanced(by: TimeInterval(SchedulePageKt.endTime)), className: SchedulePageKt.className)
            let state = CountdownAttributes.ContentState()
            
            if #available(iOS 16.1, *) {
                try? Activity<CountdownAttributes>.request(attributes: attributes, contentState: state, pushType: nil)
            } else {
                // Fallback on earlier versions
            }
        }
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView().ignoresSafeArea()
        }
    }
}
