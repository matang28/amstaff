package amstaff.core.handlers

import amstaff.core.SampleHandler
import amstaff.core.SampleResult
import amstaff.core.SampleStatus
import amstaff.core.Sampler

class EmailHandler : SampleHandler {

    override suspend fun invoke(sampler: Sampler, result: SampleResult): SampleStatus {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}