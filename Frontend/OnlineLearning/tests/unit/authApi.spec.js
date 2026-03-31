import { loginByPassword } from '@/api/authApi'

describe('authApi', () => {
  test('空参数抛出异常', async () => {
    await expect(loginByPassword({ account: '', password: '' })).rejects.toThrow(
      '账号与密码不能为空',
    )
  })

  test('正常返回 token', async () => {
    const result = await loginByPassword({ account: 'teacher@ailearning.com', password: '123456' })
    expect(result.token).toContain('token_')
    expect(result.profile.name).toBe('王老师')
  })
})
